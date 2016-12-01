package io.pivotal.cf.sample.controller;

import io.pivotal.cf.sample.HeatMap;
import io.pivotal.cf.sample.Order;
import io.pivotal.cf.sample.OrderConsumer;
import io.pivotal.cf.sample.RabbitClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Set;
import java.util.Random;

/**
 * Handles requests for the application home page.
 */
@Controller
public class OrderController {

	@Autowired
	ServletContext context;

	private static Map<String,Queue<Order>> stateOrdersMap = new HashMap<String, Queue<Order>>();



	private static RabbitClient client ;

	boolean generatingData = false;

	static Logger logger = Logger.getLogger(OrderController.class);

	OrderConsumer generator = new OrderConsumer();
	Thread threadConsumer = new Thread (generator);
	public HashMap<String, String> resultMap;

    public OrderController(){

		client = RabbitClient.getInstance();

    	for (int i=0; i<HeatMap.states.length; i++){
    		stateOrdersMap.put(HeatMap.states[i], new ArrayBlockingQueue<Order>(2));
    	}

		threadConsumer.start();

    	resultMap = new HashMap<String, String>();

		resultMap.put("ca", "blue");
		resultMap.put("ny", "red");
		resultMap.put("ma", "blue");
		resultMap.put("tx", "red");
		resultMap.put("il", "blue");
		resultMap.put("wa", "blue");
		resultMap.put("fl", "red");
		resultMap.put("pa", "red");
		resultMap.put("va", "blue");
		resultMap.put("nj", "blue");
		resultMap.put("or", "blue");
		resultMap.put("oh", "red");
		resultMap.put("mi", "red");
		resultMap.put("co", "blue");
		resultMap.put("md", "blue");
		resultMap.put("nc", "blue");
		resultMap.put("ga", "red");
		resultMap.put("mn", "blue");
		resultMap.put("az", "red");
		resultMap.put("in", "red");
		resultMap.put("wi", "red");
		resultMap.put("mo", "red");
		resultMap.put("tn", "red");
		resultMap.put("ct", "blue");
		resultMap.put("dc", "blue");
		resultMap.put("ut", "red");
		resultMap.put("nm", "blue");
		resultMap.put("ks", "red");
		resultMap.put("ky", "red");
		resultMap.put("ok", "red");
		resultMap.put("sc", "red");
		resultMap.put("la", "red");
		resultMap.put("nv", "red");
		resultMap.put("ia", "red");
		resultMap.put("nh", "blue");
		resultMap.put("al", "red");
		resultMap.put("ar", "red");
		resultMap.put("me", "blue");
		resultMap.put("hi", "red");
		resultMap.put("ne", "red");
		resultMap.put("id", "red");
		resultMap.put("ri", "blue");
		resultMap.put("vt", "blue");
		resultMap.put("mt", "red");
		resultMap.put("wv", "red");
		resultMap.put("ak", "red");
		resultMap.put("ms", "red");
		resultMap.put("wy", "red");
		resultMap.put("sd", "red");
		resultMap.put("nd", "red");
		resultMap.put("de", "blue");
		resultMap.put("pr", "red");
		resultMap.put("as", "red");
    }

/**
	private int getOrderSum(String state){

		int sum = 0;
		Queue<Order> q  = stateOrdersMap.get(state);
		Iterator<Order> it = q.iterator();
		while (it.hasNext()){
			sum += it.next().getAmount();
		}

		return sum;
	}

**/

	private String getElectionResult(String state){
		String result="white";
		Queue<Order> q  = stateOrdersMap.get(state);
		Iterator<Order> it = q.iterator();
		while (it.hasNext()){
			return result= resultMap.get(it.next().getState())	;
		}

		return result;
	}

	public static synchronized void registerOrder(Order order){
		Queue<Order> orderQueue = stateOrdersMap.get(order.getState());
		if (!orderQueue.offer(order)){
			orderQueue.remove();
			orderQueue.add(order);
		}
	}

	@RequestMapping(value = "/")
	public String home(Model model) {
		model.addAttribute("rabbitURI", client.getRabbitURI());
        return "WEB-INF/views/pcfdemo.jsp";
    }

    @RequestMapping(value="/getData")
    public @ResponseBody double getData(@RequestParam("state") String state){
		Random random = new Random();
    	//if (!stateOrdersMap.containsKey(state)) return 0;
    	//Queue<Order> q = stateOrdersMap.get(state);
    	//if (q.size()==0) return 0;
    	//Order[] orders = q.toArray(new Order[]{});

    	//return orders[orders.length-1].getAmount();
    	return (1+random.nextInt(4))*10;
    }


    @RequestMapping(value="/getCompleteData")
    public @ResponseBody Map<String,Queue<Order>> getCompleteData(){
    	return stateOrdersMap;

    }

@RequestMapping(value="/startStream")
    public @ResponseBody String startStream(){
		logger.warn("Rabbit URI "+client.getRabbitURI());
		if (client.getRabbitURI()==null) return "Please bind a RabbitMQ service";

    	if (generatingData) return "Data already being generated";

    	generatingData = true;

    	generator.startGen();
    	return "Started";

    }

    @RequestMapping(value="/stopStream")
    public @ResponseBody String stopStream(){
		logger.warn("Rabbit URI "+client.getRabbitURI());
		if (client.getRabbitURI()==null) return "Please bind a RabbitMQ service";

    	if (!generatingData) return "Not Streaming";
    	generatingData = false;
    	generator.stopGen();

    	return "Stopped";

    }

    @RequestMapping(value="/killApp")
    public @ResponseBody String kill(){
		logger.warn("Killing application instance");
		System.exit(-1);
    	return "Killed";

    }

    @RequestMapping(value="/getHeatMap")
    public @ResponseBody HeatMap getHistograms(){
		//if(!threadConsumer.isAlive()){threadConsumer.start();}
    	HeatMap heatMap = new HeatMap();
    	for (int i=0; i<HeatMap.states.length; i++){
    		heatMap.addOrderSum(HeatMap.states[i], getElectionResult(HeatMap.states[i]));
    	}

    	heatMap.assignColors();
    	return heatMap;

    }


}
