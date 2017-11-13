package io.github.jamiesanson.broker.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import io.github.jamiesanson.broker.annotation.BrokerRepo;
import io.github.jamiesanson.broker.annotation.Persistent;
import io.github.jamiesanson.broker.annotation.Transient;
import io.github.jamiesanson.broker.compiler.model.RepositoryModel;
import io.github.jamiesanson.broker.compiler.model.ResolverModel;
import io.github.jamiesanson.broker.compiler.util.Logger;

/**
 * Main Annotation Processor for Broker. Captures {@link BrokerRepo}
 * annotated classes and inspects those for annotated methods
 */
@AutoService(Processor.class)
public class BrokerAnnotationProcessor extends AbstractProcessor implements Logger {

    private static final List<Class<? extends Annotation>> BROKER_TYPES = Collections.singletonList(
            BrokerRepo.class);

    private static final List<Class<? extends Annotation>> FIELD_ANNOTATION_TYPES = Arrays.asList(
            Transient.class,
            Persistent.class
    );

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : BROKER_TYPES) {
            annotations.add(annotation.getCanonicalName());
        }
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<RepositoryModel> repositoryModels = new LinkedList<>();

        // Find all repository classes and produce list of models
        for (Element element : roundEnv.getElementsAnnotatedWith(BrokerRepo.class)) {
            // Filter the misused annotations
            if (element.getKind() != ElementKind.INTERFACE) {
                error("BrokerRepo can only be applied to an Interface.");
                return true;
            }

            TypeElement repoElement = (TypeElement) element;
            List<? extends Element> allElements = repoElement.getEnclosedElements();
            List<ExecutableElement> methodElements = ElementFilter.methodsIn(allElements);

            // Filter elements not annotated with supported annotations
            for (ExecutableElement method : methodElements) {
                if (!isMethodAnnotated(method)) {
                    methodElements.remove(method);
                }
            }

            repositoryModels.add(new RepositoryModel(repoElement, methodElements, this));
        }

        if (!repositoryModels.isEmpty()) {
            log("Processing " + repositoryModels.size() + " repositories");
            for (RepositoryModel model : repositoryModels) {
                processRepoModel(model);
            }

            processResolver(repositoryModels);
        }

        return true;
    }

    private void processRepoModel(RepositoryModel model) {
        try {
            JavaFile.builder(this.getClass().getPackage().getName(), model.generateSpec(true).build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void processResolver(List<RepositoryModel> models) {
        try {
            JavaFile.builder(this.getClass().getPackage().getName(), new ResolverModel(models, this).generateSpec().build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private boolean isMethodAnnotated(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        List<String> supportedNames = new ArrayList<>();

        // Add supported annotation names
        for (Class<? extends Annotation> annotation : FIELD_ANNOTATION_TYPES) {
            supportedNames.add(annotation.getSimpleName());
        }

        int supportedAnnotationsFound = 0;

        // Filter non-supported annotations
        for (AnnotationMirror mirror : mirrors) {
            String annotationName = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (supportedNames.contains(annotationName)) {
                supportedAnnotationsFound += 1;
            }
        }

        if (supportedAnnotationsFound > 1) {
            error("Cannot have multiple annotations on element: " + element.getSimpleName());
        }

        return supportedAnnotationsFound == 1;
    }

    @Override
    public void error(@NotNull String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, "[BROKER]: " + error);
    }

    @Override
    public void warn(@NotNull String warning) {
        messager.printMessage(Diagnostic.Kind.WARNING, "[BROKER]: " + warning);
    }

    @Override
    public void log(@NotNull String log) {
        messager.printMessage(Diagnostic.Kind.NOTE, "[BROKER]: " + log);
    }
}
