package fr.euphyllia.worldguardflagpanel;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("UnstableApiUsage")
public final class WorldGuardFlagPanelLoader implements PluginLoader {

    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.euphyllia.moe/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("faststatsReleases", "default", "https://repo.faststats.dev/releases").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("com.electronwill.night-config:toml:3.8.4"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.faststats.metrics:bukkit:0.24.1"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
