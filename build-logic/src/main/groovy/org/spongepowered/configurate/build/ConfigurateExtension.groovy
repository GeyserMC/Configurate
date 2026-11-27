package org.spongepowered.configurate.build

import groovy.transform.PackageScope
import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

class ConfigurateExtension {

    /**
     * The Maven group upstream Configurate publishes its modules under.
     */
    static final String UPSTREAM_GROUP = 'org.spongepowered'

    /**
     * Whether this module is a drop-in replacement for the upstream module of
     * the same name, sharing its Java packages.
     *
     * <p>If so, it declares the upstream module's coordinates as a Gradle
     * capability, so consumers that end up with both on their classpath get a
     * capability conflict to resolve instead of silently shipping duplicate
     * classes.</p>
     *
     * <p>Modules that only exist in this fork, or that use their own Java
     * packages, should set this to {@code false}.</p>
     */
    boolean replacesUpstream = true

    @PackageScope VersionCatalog libs
    private final DependencyHandler dh

    ConfigurateExtension(final DependencyHandler dh) {
        this.dh = dh
    }

    void useAutoValue() {
        if (!libs) {
            throw new GradleException('Tried to useAutoValue() without the Java plugin present! libs is not set.')
        }
        this.dh.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, libs.findLibrary('autoValue-annotations').get())
        this.dh.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, libs.findLibrary('autoValue').get())
    }

    void applyCommonAttributes(final Javadoc self) {
        def options = self.options
        options.encoding = 'UTF-8'
        if (options instanceof StandardJavadocDocletOptions) {
            options.links(
                'https://lightbend.github.io/config/latest/api/',
                'https://fasterxml.github.io/jackson-core/javadoc/2.10/',
                // "https://javadocs.dev/org.checkerframework/checker-qual/${libs.findVersion('checkerQual').get().requiredVersion}/",
                // "https://javadocs.dev/io.leangen.geantyref/geantyref/${libs.findVersion('geantyref').get().requiredVersion}/"
            )

            options.addBooleanOption('html5', true)
            options.addStringOption('-release', '8')
            options.linkSource()
        }
    }

}
