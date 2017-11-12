package io.github.jamiesanson.broker.compiler;

import com.google.auto.service.AutoService;
import com.sun.xml.internal.bind.v2.TODO;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import io.github.jamiesanson.broker.annotation.BrokerRepo;
import io.github.jamiesanson.broker.annotation.Persistent;
import io.github.jamiesanson.broker.annotation.Transient;
import io.github.jamiesanson.broker.compiler.model.RepositoryModel;

@AutoService(Processor.class)
public class BrokerAnnotationProcessor extends AbstractProcessor {

    private static final List<Class<? extends Annotation>> BROKER_TYPES = Arrays.asList(
            BrokerRepo.class,
            Transient.class,
            Persistent.class);

    private Messager messager;
    private Types typesUtil;
    private Elements elementsUtil;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        typesUtil = processingEnv.getTypeUtils();
        elementsUtil = processingEnv.getElementUtils();
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
            if (element.getKind() != ElementKind.CLASS) {
                error("BrokerRepo can only be applied to a class.");
                return true;
            }

            TypeElement repoElement = (TypeElement) element;
            List<? extends Element> allElements = repoElement.getEnclosedElements();

            // Filter out elements that aren't fields and add to the repositoryModel list
            repositoryModels.add(new RepositoryModel(repoElement, ElementFilter.fieldsIn(allElements)));
        }

        // TODO: Process each repository to form classes, then use Spoon to modify BrokerRepository to add getters
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void error(String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, error);
    }

    @SuppressWarnings("SameParameterValue")
    private void warn(String warning) {
        messager.printMessage(Diagnostic.Kind.WARNING, warning);
    }

    @SuppressWarnings("SameParameterValue")
    private void log(String log) {
        messager.printMessage(Diagnostic.Kind.NOTE, log);
    }
}
