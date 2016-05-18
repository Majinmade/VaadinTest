package com.example.vaadintest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("vaadintest")
public class VaadintestUI extends UI {
	
	static {
	    SLF4JBridgeHandler.install();
	  }

	private ScheduledExecutorService timer = Executors
			.newScheduledThreadPool(1);
	
	private ScheduledFuture<?> scheduledFuture;

	private long index = 0;

	private final static Logger logger =
	          Logger.getLogger(VaadintestUI.class.getName());

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = VaadintestUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);
		
		setPollInterval(1000);

		final TextArea output = new TextArea();
		output.setSizeFull();
		output.setReadOnly(true);
		
		final TextArea error = new TextArea();
		error.setSizeFull();
		error.setReadOnly(true);

		final Button start = new Button("Start");
		
		final Button test = new Button("Test");
		
		//final File f = new File("/share/MD0_DATA/.qpkg/Tomcat/tomcat/logs/catalina.out");
		final File f = new File("D:\\tmp\\test.txt");
		
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					BufferedReader fr = new BufferedReader(new FileReader(f));
					String outString = output.getValue();
					fr.skip(index);
					while (fr.ready()) {
						String line = fr.readLine();
						outString += line + "\n";
						index += line.length() + 2;
					}
					fr.close();
					output.setReadOnly(false);
					output.setValue(outString);
					output.setReadOnly(true);
					output.setSelectionRange(output.getValue().length()-1, 1);
				} catch (IOException e) {
					error.setReadOnly(false);
					error.setValue(e.getMessage());
					error.setReadOnly(true);
				}

			}
		};
		
		start.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (scheduledFuture == null || scheduledFuture.isCancelled()) {
					scheduledFuture = timer.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
					start.setCaption("Stop");
				} else {
					start.setCaption("Start");
					scheduledFuture.cancel(true);
				}
			}
		});
		
		test.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				logger.log(Level.WARNING, "Test");
			}
		});

		layout.addComponent(output);
		layout.addComponent(start);
		layout.addComponent(test);
		layout.addComponent(error);
	}

}