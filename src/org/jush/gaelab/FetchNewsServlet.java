package org.jush.gaelab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jush.gaelab.model.NewsItem;
import org.jush.gaelab.model.PMF;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.appengine.api.datastore.Text;

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
      parseFeedandPersist(totalFeed, resp);
    } catch (MalformedURLException e) {
      // ...
    } catch (IOException e) {
      // ...
    }
  }

  private void parseFeedandPersist(String feedContent, HttpServletResponse resp) throws IOException {
    Document doc = parseXml(feedContent);
    NodeList titles = null;
    NodeList links = null;
    NodeList descriptions = null;

    if (doc != null) {
      resp.getWriter().println("node: " + doc.getDocumentElement().getNodeName());
      titles = doc.getDocumentElement().getElementsByTagName("title");
      links = doc.getDocumentElement().getElementsByTagName("link");
      descriptions = doc.getDocumentElement().getElementsByTagName("description");
    } else {
      resp.getWriter().println("no input/bad xml input. please send parameter content=<xml>");
    }

    persistNewsData(titles, links, descriptions);

    resp.getWriter().println("<h3>Successfully fetched the following news from feed.</h3>");
    for (int i = 1; i < titles.getLength(); i++) {
      resp.getWriter().println("<br/>Title: " + titles.item(i).getTextContent());
      resp.getWriter().println("<br/>Link: <a href=\"" + links.item(i).getTextContent() + "\">" + titles.item(i).getTextContent() + "</a>");
      if (descriptions.item(i) != null) {
        resp.getWriter().println("<br/>Description: " + descriptions.item(i).getTextContent());
      }

      resp.getWriter().println("<br/><br/>");
    }
  }

  private static Document parseXml(String strXml) {
    Document doc = null;
    String strError;
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();

      StringReader reader = new StringReader(strXml);
      InputSource inputSource = new InputSource(reader);

      doc = db.parse(inputSource);

      return doc;
    } catch (IOException ioe) {
      strError = ioe.toString();
    } catch (ParserConfigurationException pce) {
      strError = pce.toString();
    } catch (SAXException se) {
      strError = se.toString();
    } catch (Exception e) {
      strError = e.toString();
    }
    return null;
  }

  private void persistNewsData(NodeList titles, NodeList links, NodeList descriptions) {
    // Persists news data feed.
    // Also clears out previously persisted news feed data

    PersistenceManager pm = PMF.get().getPersistenceManager();
    javax.jdo.Query query = pm.newQuery(NewsItem.class);
    Long res = query.deletePersistentAll();

    System.out.println("Datastore deleted  " + res + "records");

    pm = PMF.get().getPersistenceManager();

    try {
      for (int i = 1; i < titles.getLength(); i++) {
        NewsItem ni = new NewsItem();
        ni.setTitle(titles.item(i).getTextContent());
        ni.setLink(links.item(i).getTextContent());
        if (descriptions.item(i) != null) {
          ni.setDescription(new Text(descriptions.item(i).getTextContent()));
        }
        pm.makePersistent(ni);
      }
    } finally {
      pm.close();
    }
  }
}
