package io.pivotal.cf.sample;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rabbitmq.client.QueueingConsumer;

import io.pivotal.cf.sample.controller.OrderController;

import java.util.logging.Logger;


public class OrderConsumer implements Runnable {

	JSONParser parser;

    private final Logger LOG = Logger.getLogger(OrderConsumer.class.getName());

	private boolean generating = false;
	private boolean stopped = false;

	public void startGen(){
		this.generating = true;
	}

	public void stopGen(){
		this.generating = false;
	}


	public OrderConsumer(){

		 parser = new JSONParser();

	}

	@Override
	public void run() {

		RabbitClient client = RabbitClient.getInstance();
		int limit=0;
		try{
			QueueingConsumer consumer = client.consumeOrders();
			while (!stopped){

				if (generating){

				try{
			      QueueingConsumer.Delivery delivery = consumer.nextDelivery();

			      String message = new String(delivery.getBody());

			      JSONObject obj = (JSONObject) parser.parse(message);

			      Order order = new Order();
			      //order.setAmount(((Number)obj.get("amount")).intValue());
			      order.setState((String)obj.get("state"));
			      //order.setResult((String)obj.get("result"));

			      OrderController.registerOrder(order);
			      limit++;
			      if (limit==53){generating = false;}

				}
				catch(Exception e){
					LOG.warning(e.getMessage());
					try{
						Thread.sleep(800);
						}
					catch(Exception ex){}
					// re-create the connection to the queue

					try{
						consumer = client.consumeOrders();
					}
					catch(Exception ex){LOG.warning(ex.getMessage());}

				}
			}
			else{
				try{
					Thread.sleep(1000);
				}catch(Exception e){ LOG.warning(e.getMessage()); }
				}

				try{
					Thread.sleep(500);
					}
					catch(Exception ex){}
			}

		}
		catch(Exception e){
			LOG.warning(e.getMessage());
			System.out.println("consume --------------- error");
			LOG.throwing(this.getClass().getName(),"consume", e);
			throw new RuntimeException(e);
		}

	}

}
