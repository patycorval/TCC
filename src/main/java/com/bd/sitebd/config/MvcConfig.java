package com.bd.sitebd.config; // Verifique se este é o seu pacote de configuração

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		String resourcePath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

		// 3. Mapeia a URL pública "/uploads/**" para o diretório físico
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(resourcePath);

		System.out.println("--- Mapeamento de Recursos Estáticos (MvcConfig) ---");
		System.out.println("URL pública: /uploads/**");
		System.out.println("Mapeada para pasta física: " + resourcePath);
		System.out.println("-----------------------------------------------------");
	}
}