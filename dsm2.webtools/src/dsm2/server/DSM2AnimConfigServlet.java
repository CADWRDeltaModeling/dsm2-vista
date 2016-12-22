package dsm2.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DSM2AnimConfigServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = getServletContext().getRealPath("WEB-INF/../animconfigs");
		File dirAnimConfigs = new File(path);
		String[] configFiles = dirAnimConfigs.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".animconfig")) {
					return true;
				} else {
					return false;
				}
			}
		});
		resp.setContentType("application/json");
		PrintWriter wr = resp.getWriter();
		wr.print("[");
		if (configFiles != null) {
			for (int i = 0; i < configFiles.length; i++) {
				String config = loadConfig(dirAnimConfigs,configFiles[i]);
				if (config == null) {
					continue;
				}
				wr.print(config);
				if (i != configFiles.length - 1) {
					wr.println(",");
				}
			}
		}
		wr.print("]");
		wr.flush();

	}

	public String loadConfig(File dir, String file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(dir,file)));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			if (reader != null)
				reader.close();
			return sb.toString();
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
