package app.ciserver;

import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	private static final String ENV = System.getenv("JAVA_ENV");

	public static void main(String[] args) {

		Javalin app = Javalin.create(config -> {
			config.jsonMapper(new JavalinJackson());
			config.fileRenderer(new JavalinThymeleaf(thymeleafEngine()));
		}).start(7000);

	}

	public static TemplateEngine thymeleafEngine() {
		AbstractConfigurableTemplateResolver templateResolver;
		if ("dev".equalsIgnoreCase(ENV)) {
			// In dev, use FileTemplateResolver for easy template reloading
			templateResolver = new FileTemplateResolver();
			String templatePath = Paths.get("src/main/resources/templates/").toAbsolutePath().toString();
			templateResolver.setPrefix(templatePath + "/"); // Use absolute file path
			templateResolver.setCacheable(false);
		} else {
			// In prod, use ClassLoaderTemplateResolver for performance
			templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("/templates/");
			templateResolver.setCacheable(true);
		}

		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setSuffix(".html");

		final var templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);

		return templateEngine;
	}
}
