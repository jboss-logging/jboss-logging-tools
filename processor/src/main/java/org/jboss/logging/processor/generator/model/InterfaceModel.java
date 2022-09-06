package org.jboss.logging.processor.generator.model;

import org.jboss.jdeparser.*;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.jboss.jdeparser.JMod.FINAL;
import static org.jboss.jdeparser.JTypes.$t;
import static org.jboss.jdeparser.JTypes.typeOf;

public abstract class InterfaceModel {

    protected final String className;

    final JSourceFile sourceFile;

    final ProcessingEnvironment processingEnv;

    private final JSources sources;

    private final JClassDef classDef;

    private final MessageInterface messageInterface;

    private final String superClassName;

    private final String format;

    private final Map<String, JMethodDef> messageMethods;

    InterfaceModel(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface,
                   final String className, final String superClassName) {
        this.processingEnv = processingEnv;
        this.messageInterface = messageInterface;
        // this.className = messageInterface.packageName() + "." + className;
        this.className = className;
        this.superClassName = superClassName;
        sources = JDeparser.createSources(JFiler.newInstance(processingEnv.getFiler()),
                                          new FormatPreferences(new Properties()));
        sourceFile = sources.createSourceFile(messageInterface.packageName(), className);
        classDef = sourceFile._interface(JMod.PUBLIC, className);
        final int idLen = messageInterface.getIdLength();
        if (idLen > 0) {
            format = "%s%0" + messageInterface.getIdLength() + "d: %s";
        } else {
            format = "%s%d: %s";
        }
        messageMethods = new HashMap<>();
    }

    /**
     * Returns the message interface being used.
     *
     * @return the message interface.
     */
    public final MessageInterface messageInterface() {
        return messageInterface;
    }

    /**
     * Writes the generated source file to the file system.
     *
     * @throws java.io.IOException if the file could not be written
     */
    public final void generateAndWrite() throws IOException {
        generateModel();
        sources.writeSources();
        JDeparser.dropCaches();
    }

    /**
     * Generate the code corresponding to this
     * class model
     *
     * @return the generated code
     * @throws IllegalStateException if the class has already been defined.
     */
    JClassDef generateModel() throws IllegalStateException {
        // Add generated annotation if required
        final TypeElement generatedAnnotation = messageInterface.generatedAnnotation();
        if (generatedAnnotation != null) {
            final JType generatedType = typeOf(generatedAnnotation.asType());
            sourceFile._import(generatedType);
            classDef.annotate(generatedType)
                    .value("value", getClass().getName())
                    .value("date", JExprs.str(ClassModelHelper.generatedDateValue()));
        }

        // Create the default JavaDoc
        classDef.docComment()
                .text("Warning this class consists of generated code.");

        // Add extends
        classDef._extends(typeOf(messageInterface.asType()));

        return classDef;
    }

    JMethodDef addMessageMethod(final MessageMethod messageMethod, final String messageMethodName, final String messageValue) {
        // Values could be null and we shouldn't create message methods for null values.
        if (messageValue == null) {
            return null;
        }

        // Create the method that returns the string message for formatting
        JMethodDef method = messageMethods.get(messageMethodName);
        if (method == null) {
            method = classDef.method(JMod.DEFAULT, String.class, messageMethodName);
            final JBlock body = method.body();
            final String msg;
            if (messageInterface.projectCode() != null && !messageInterface.projectCode().isEmpty() && messageMethod.message().hasId()) {
                // Prefix the id to the string message
                msg = String.format(format, messageInterface.projectCode(), messageMethod.message().id(), messageValue);
            } else {
                msg = messageValue;
            }
            body._return(JExprs.str(msg));
            messageMethods.put(messageMethodName, method);
        }

        return method;
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public final String qualifiedClassName() {
        return className;
    }

    JMethodDef addI18nMessageMethod(final MessageMethod messageMethod, Set<String> locales) {
        // Create the method that returns the string message for formatting
        JMethodDef method = messageMethods.get(messageMethod.messageMethodName());
        if (method == null) {
            method = classDef.method(JMod.DEFAULT, String.class, messageMethod.messageMethodName());
            final JBlock body = method.body();

            JVarDeclaration language = body.var(FINAL, $t(String.class), "language", JExprs.call("language"));

            if (locales != null) {
                for (String locale : locales) {
                    body._if(JExprs.str(locale).call("endsWith").arg(JExprs.$v(language)))._return(JExprs.call(messageMethod.messageMethodName() +locale));
                }
            }

            final String msg;
            if (messageInterface.projectCode() != null && !messageInterface.projectCode()
                                                                           .isEmpty() && messageMethod.message()
                                                                                                      .hasId()) {
                // Prefix the id to the string message
                msg = String.format(format, messageInterface.projectCode(), messageMethod.message()
                                                                                         .id(), messageMethod.message()
                                                                                                             .value());
            } else {
                msg = messageMethod.message()
                                   .value();
            }
            body._return(JExprs.str(msg));
            messageMethods.put(messageMethod.messageMethodName(), method);
        }

        return method;
    }

}
