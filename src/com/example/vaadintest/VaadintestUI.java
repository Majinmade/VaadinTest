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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.servlet.annotation.WebServlet;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
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

	private ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);

	private ScheduledFuture<?> scheduledFuture;

	private long index = 0;

	private String outString;

	private boolean follow = true;

	private File f;

	private final static Logger logger = Logger.getLogger(VaadintestUI.class.getName());

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = VaadintestUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		GridLayout layout = new GridLayout(3, 3);
		layout.setMargin(true);
		layout.setHeight(100, Unit.PERCENTAGE);
		layout.setWidth(100, Unit.PERCENTAGE);
		setContent(layout);

		setPollInterval(100);

		final TextArea output = new TextArea();
		output.setSizeFull();
		output.setReadOnly(true);

		final TextArea error = new TextArea();
		error.setSizeFull();
		error.setReadOnly(true);

		final Button start = new Button("Start");

		final Button test = new Button("Test");

		final Button followButton = new Button("Stop Follow");

		if (getUI().getPage().getLocation().getHost().contains("localhost")) {
			f = new File("D:\\tmp\\test.txt");
		} else {
			f = new File("/share/Qmisc/Wildfly/standalone/log/server.log");
		}

		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					BufferedReader fr = new BufferedReader(new FileReader(f));
					outString = output.getValue();
					Stream<String> lines = fr.lines();
					lines.skip(index).forEach(line -> {
						outString += line + "\n";
						index++;
					});
					fr.close();
					synchronized (VaadintestUI.this) {
						output.setReadOnly(false);
						output.setValue(outString);
						output.setReadOnly(true);
						if (follow) {
							output.setSelectionRange(output.getValue().length() - 1, 1);
						}
					}
				} catch (Exception e) {
					synchronized (VaadintestUI.this) {
						error.setReadOnly(false);
						error.setValue(e.getMessage());
						error.setReadOnly(true);
					}
				}

			}
		};

		start.addClickListener(listener -> {
			if (scheduledFuture == null || scheduledFuture.isCancelled()) {
				scheduledFuture = timer.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);
				start.setCaption("Stop");
			} else {
				start.setCaption("Start");
				scheduledFuture.cancel(true);
			}
		});

		test.addClickListener(listener -> {
			logger.log(Level.WARNING, "Test");
		});

		followButton.addClickListener(listener -> {
			if (follow) {
				followButton.setCaption("Follow");
			} else {
				followButton.setCaption("Stop Follow");
			}
			follow = !follow;
		});

		layout.addComponent(output, 0, 0, 2, 0);
		layout.addComponent(start, 0, 1);
		layout.addComponent(test, 1, 1);
		layout.addComponent(followButton, 2, 1);
		layout.addComponent(error, 0, 2, 2, 2);
	}

}