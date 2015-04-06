package apt.test.processors;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import apt.test.annotations.Path;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class MVCProcessor extends AbstractProcessor {

	private final static String pkgName = "apt.test";
	private final static String className = "Generated";
	
	private List<String> routes;

	public MVCProcessor() {
		super();
		this.routes = new ArrayList<String>();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for(Element el : roundEnv.getElementsAnnotatedWith(Path.class)) {
			Path path = el.getAnnotation(Path.class);
			routes.add(path.value());
		}
		if (!routes.isEmpty()) {
			Filer filer = processingEnv.getFiler();
			Writer processorWriter = null;
			try {
				JavaFileObject processorFO = filer.createSourceFile(pkgName+"." +className);
				processorWriter = processorFO.openWriter();
				Properties props = new Properties();
                URL url = this.getClass().getClassLoader().getResource("velocity.properties");
                props.load(url.openStream());				
                VelocityEngine ve = new VelocityEngine(props);
				VelocityContext vc = new VelocityContext();
				vc.put("packageName", pkgName);
				vc.put("className", className);
				vc.put("routes", routes);
				Template vt = ve.getTemplate("VertxMVC.vm");
				vt.merge(vc, processorWriter);
			}
			catch (Exception e) {
			}
			finally {
				safeClose(processorWriter);
			}
		}		
		return true;
	}

	private void safeClose(Closeable closeable)
	{
		if (closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch (IOException ignore)
			{
			}
		}
	}

}
