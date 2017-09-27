# 一步一步，实现自己的ButterKnife
ButterKnife插件的出现让Android程序员从繁琐的findViewById重复代码中解放出来，尤其搭配各种自动生成代码的Android Studio插件，更是如虎添翼。
ButterKnife的实现原理，大家应该都有所耳闻，利用AbstractProcess，在编译时候为BindView注解的控件自动生成findViewById代码，ButterKnife#bind(Activity)方法，实质就是去调用自动生成的这些findViewById代码。
然而，当我需要去了解这些实现细节的时候，我决定去看看ButterKnife的源码。ButterKnife整个项目涵盖的注解有很多，看起来可能会消耗不少的时间，笔者基于这些天的摸索的该项目的思路，实现了自己的一个BindView注解的使用，来帮助大家了解。
## GitHub链接
笔者实现的项目已经上传到Github，欢迎大家star。[点击查看MyButterKnife](https://github.com/lizhengxian1991/MyButterKnife)
## 项目结构
### Annotation module
我们需要处理的BindView注解，就声明在这个module里，简单不多说。
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface BindView {
    int value() default -1;
}
```
Target为FIELD类型，表示这个注解用于类内属性的声明；Retention为CLASS，表示这个注解在项目编译时起作用，如果为RUNTIME则表示在运行时起作用，RUNTIME的注解都是结合反射使用的，所以执行效率上有所欠缺，应该尽量避免使用RUNTIME类注解。
BindView内的value为int类型，正是R.id对应的类型，方便我们直接对View声明其绑定的id：
```java
@BindView(R.id.btn)
protected Button mBtn;
```
### Compiler module
这个module是自动生成findViewById代码的重点，这里只有一个类，继承于AbstractProcessor。
```java
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BindProcess extends AbstractProcessor{
    private Elements mElementsUtil;

    /**
     * key:     eclosed elemnt
     * value:   inner views with BindView annotation
     */
    private Map<TypeElement,Set<Element>> mElems;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementsUtil = processingEnv.getElementUtils();
        mElems = new HashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("Process start !");

        initBindElems(roundEnv.getElementsAnnotatedWith(BindView.class));
        generateJavaClass();

        System.out.println("Process finish !");
        return true;
    }

    private void generateJavaClass() {
        for (TypeElement enclosedElem : mElems.keySet()) {
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.get(enclosedElem.asType()),"activity")
                    .returns(TypeName.VOID);
            for (Element bindElem : mElems.get(enclosedElem)) {
                methodSpecBuilder.addStatement(String.format("activity.%s = (%s)activity.findViewById(%d)",bindElem.getSimpleName(),bindElem.asType(),bindElem.getAnnotation(BindView.class).value()));
            }
            TypeSpec typeSpec = TypeSpec.classBuilder("Bind"+enclosedElem.getSimpleName())
                    .superclass(TypeName.get(enclosedElem.asType()))
                    .addModifiers(Modifier.FINAL,Modifier.PUBLIC)
                    .addMethod(methodSpecBuilder.build())
                    .build();
            JavaFile file = JavaFile.builder(getPackageName(enclosedElem),typeSpec).build();
            try {
                file.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initBindElems(Set<? extends Element> bindElems) {
        for (Element bindElem : bindElems) {
            TypeElement enclosedElem = (TypeElement) bindElem.getEnclosingElement();
            Set<Element> elems = mElems.get(enclosedElem);
            if (elems == null){
                elems=  new HashSet<>();
                mElems.put(enclosedElem,elems);
                System.out.println("Add enclose elem "+enclosedElem.getSimpleName());
            }
            elems.add(bindElem);
            System.out.println("Add bind elem "+bindElem.getSimpleName());
        }
    }

    private String getPackageName(TypeElement type) {
        return mElementsUtil.getPackageOf(type).getQualifiedName().toString();
    }
}
```
类注解@AutoServic用于自动生成META-INF信息，对于AbstractProcessor的继承类，需要声明在META-INF里，才能在编译时生效。有了AutoService，可以自动把注解的类加入到META-INF里。使用AutoService需要引入如下包：
```java
compile 'com.google.auto.service:auto-service:1.0-rc2'
```
然后编译时就会执行proces方法来生成代码，参数annotautions是一个集合，由于上面getSupportedAnnotationTypes返回的是@BindView注解，所以annotations参数里包含所有被@BindView注解的元素。把各元素按照所在类来分组，放入map中，然后generateJavaClass方法中用该map来生成代码，这里使用了javapoet包里的类，能很方便的生成各种java类，方法，修饰符等等。方法体类代码看似复杂，但稍微学一下javapoet包的使用，就可以很快熟练该方法的作用，以下是编译后生成出来的java类代码：
```java
package top.lizhengxian.apt_sample;

public final class BindMainActivity extends MainActivity {
  public static void bindView(MainActivity activity) {
    activity.mBtn = (android.widget.Button)activity.findViewById(2131427422);
    activity.mTextView = (android.widget.TextView)activity.findViewById(2131427423);
  }
}
```
而被注解的原类如下：
```java
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    protected Button mBtn;

    @BindView(R.id.text)
    protected TextView mTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BindMainActivity.bindView(this)
    }
}
```
生成的java类位于如下位置：
![](https://user-gold-cdn.xitu.io/2017/9/24/a863de7339e854cfcfabd8c271a97fdb)
### mybutterknife module
按理说，上面已经完成了整个findViewById的代码生成，在MainActivity的onCreat方法里，调用完setContentView后，就可以直接调用BindMainActivity.bindView(this)来完成各个View和id的绑定和实例化了。
但是我们观察ButterKnife中的实现，不管是哪个Activity类，都是调用ButterKnife.bindView(this)方法来注入的。而在本项目的代码中，不同的类，就会生成不同名字继承类，比如，如果另有一个HomeActivity类，那注入就要使用BindHomeActivity.bindView(this)来实现。
怎样实现ButterKnife那样统一方法来注入呢？
还是查看源码，可以发现，ButterKnife.bindView方法使用的还是反射来调用生成的类中的方法，也就是说，ButterKnife.bindView只是提供了统一入口。
对照于此，在mybutterknife module里，我们也可以用反射实现类似的方法路由，统一所有的注入方法入口：
```java
public class MyButterKnife {
    private static Map<Class,Method> classMethodMap = new HashMap<>();
    public static void bindView(Activity target){
        if (target != null){
            Method method = classMethodMap.get(target.getClass());
            try {
                if (method == null) {
                    String bindClassName = target.getPackageName() + ".Bind" + target.getClass().getSimpleName();
                    Class bindClass = Class.forName(bindClassName);
                    method = bindClass.getMethod("bindView", target.getClass());
                    classMethodMap.put(target.getClass(), method);
                }
                method.invoke(null, target);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
```
### sample module
综上，轻轻松松实现了我们自己的BindView注解，使用方式如下：
```java
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn)
    protected Button mBtn;

    @BindView(R.id.text)
    protected TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyButterKnife.bindView(this);
        mBtn.setText("changed");
        mTextView.setText("changed too");
    }
}
```
运行代码，完美！
