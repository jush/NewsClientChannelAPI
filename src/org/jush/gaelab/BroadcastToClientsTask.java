package org.jush.gaelab;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.jush.gaelab.model.ChannelClient;
import org.jush.gaelab.model.PMF;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class BroadcastToClientsTask implements DeferredTask {
  /**
   * 
   */
  private static final long serialVersionUID = -3835698612235064189L;

  private static final Logger log = Logger.getLogger(BroadcastToClientsTask.class.getName());

  private String title;
  private String link;
  private String description;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BroadcastToClientsTask(String title, String link, String description) {
    this.title = title;
    this.link = link;
    if (description != null) {
      this.description = description;
    }
  }

  public void run() {
    if (this.getTitle() != null && this.getLink() != null) {
      broadcastToClients(this.getTitle(), this.getLink(), this.getDescription());
      log.info("Broadcasted news message to clients");
    }
  }

  private void broadcastToClients(String title, String link, String description) {
    JSONObject jsonMessage = null;

    // Get all channel client ids available
    String query = "select from " + ChannelClient.class.getName();
    PersistenceManager pm = PMF.get().getPersistenceManager();
    @SuppressWarnings("unchecked")
    List<ChannelClient> ids = (List<ChannelClient>) pm.newQuery(query).execute();

    ChannelService channelService = ChannelServiceFactory.getChannelService();

    for (ChannelClient m : ids) {
      String client = m.getClientId();
      try {
        jsonMessage = new JSONObject();
        jsonMessage.put("title", title);
        jsonMessage.put("link", link);
        jsonMessage.put("description", description);

        System.out.println("sending json stream: " + jsonMessage.toString());
        System.out.println("to client: " + client);

        channelService.sendMessage(new ChannelMessage(client, jsonMessage.toString()));

      } catch (JSONException e) {
        e.printStackTrace();
      } catch (ChannelFailureException e) {
        e.printStackTrace();
      }
    }
    pm.close();
  }
}
