package org.jush.gaelab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FetchNewsServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/html");
    resp.getWriter().println("Getting feed...<br/>");
    String googleFeed = "http://feed.dilbert.com/dilbert/daily_strip";
    // String googleFeed =
    // "http://news.google.com/news?ned=us&topic=h&output=rss";
    String totalFeed = "";
    try {
      URL url = new URL(googleFeed);
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      String line = null;
      while ((line = reader.readLine()) != null) {
        totalFeed += line;
        System.out.println(totalFeed);
      }
      reader.close();
      // parseFeedandPersist(totalFeed, resp);
    } catch (MalformedURLException e) {
      // ...
    } catch (IOException e) {
      // ...
    }
  }
}
