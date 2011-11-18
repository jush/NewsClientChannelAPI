package org.jush.gaelab;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jush.gaelab.model.ChannelClient;
import org.jush.gaelab.model.PMF;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

@SuppressWarnings("serial")
public class TokenServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");

    ChannelService channelService = ChannelServiceFactory.getChannelService();
    
    // Use random number for client id
    Random randomGenerator = new Random();
    String clientid = Integer.toString(randomGenerator.nextInt(100000));
    String token = channelService.createChannel(clientid);
    persistId(clientid);
    resp.getWriter().println(token);
  }

  private void persistId(String clientid) {

    ChannelClient client = new ChannelClient();
    client.setClientId(clientid);
    client.setTimestamp(new Date());
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(client);
    } finally {
      pm.close();
    }
  }
}