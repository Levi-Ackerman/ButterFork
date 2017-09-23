package com.example;

import com.google.auto.service.AutoService;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BindProcess extends AbstractProcessor{
    private Elements mElementsUtil;

    /**
     * key:     eclosed elemnt
     * value:   inner views with BindView annotation
     */
    private Map<Element,Set<Element>> mElems;

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
        System.out.println("Process finish !");
        return false;
    }

    private void initBindElems(Set<? extends Element> bindElems) {
        for (Element bindElem : bindElems) {
            Element enclosedElem = bindElem.getEnclosingElement();
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
}
