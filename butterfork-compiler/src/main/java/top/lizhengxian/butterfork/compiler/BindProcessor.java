package top.lizhengxian.butterfork.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import top.lizhengxian.butterfork.annotation.BindLayout;
import top.lizhengxian.butterfork.annotation.BindView;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BindProcessor extends AbstractProcessor{
    private Elements mElementsUtil;

    /**
     * key:     eclosed elemnt
     * value:   inner views with BindView annotation
     */
    private Map<TypeElement,Set<Element>> mBindViewElems;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementsUtil = processingEnv.getElementUtils();
        mBindViewElems = new HashMap<>();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(BindLayout.class.getCanonicalName());
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
        for (TypeElement enclosedElem : mBindViewElems.keySet()) {
            //generate bind method
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.get(enclosedElem.asType()),"activity")
                    .returns(TypeName.VOID);

            BindLayout bindLayoutAnno = enclosedElem.getAnnotation(BindLayout.class);
            if (bindLayoutAnno != null){
                methodSpecBuilder.addStatement(String.format(Locale.US,"activity.setContentView(%d)",bindLayoutAnno.value()));
            }

            for (Element bindElem : mBindViewElems.get(enclosedElem)) {
                methodSpecBuilder.addStatement(String.format(Locale.US,"activity.%s = (%s)activity.findViewById(%d)",bindElem.getSimpleName(),bindElem.asType(),bindElem.getAnnotation(BindView.class).value()));
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
//                e.printStackTrace();
            }
        }
    }

    private void initBindElems(Set<? extends Element> bindElems) {
        for (Element bindElem : bindElems) {
            TypeElement enclosedElem = (TypeElement) bindElem.getEnclosingElement();
            Set<Element> elems = mBindViewElems.get(enclosedElem);
            if (elems == null){
                elems=  new HashSet<>();
                mBindViewElems.put(enclosedElem,elems);
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
