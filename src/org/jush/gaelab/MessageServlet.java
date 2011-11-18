package org.jush.gaelab;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jush.gaelab.model.NewsItem;
import org.jush.gaelab.model.PMF;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;

public class MessageServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(MessageServlet.class.getName());
  public static final long DELAY = 3000;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().print("starting messages");

    PersistenceManager pm = PMF.get().getPersistenceManager();

    // Get all current news Items
    String query = "select from " + NewsItem.class.getName();
    List<NewsItem> newsitems = (List<NewsItem>) pm.newQuery(query).execute();

    // Loop through current news items and broadcast to clients
    int i = 0;
    for (NewsItem ni : newsitems) {
      i++;
      if (ni.getTitle() != null && ni.getLink() != null) {
        String description = null;
        if (ni.getDescription() != null) {
          description = ni.getDescription().getValue();
        }

        TaskOptions taskOptions = TaskOptions.Builder.withDefaults().countdownMillis(DELAY * i)
            .payload(new BroadcastToClientsTask(ni.getTitle(), ni.getLink(), description));

        try {
          QueueFactory.getDefaultQueue().add(taskOptions);
        } catch (TaskAlreadyExistsException e) {
          e.printStackTrace();
        }
      }
    }
    pm.close();
  }
}
