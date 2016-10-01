package com.dev.jul;

import com.dev.obj.Call;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;

public class RestOperation {
	

	
	public static void sendReq(URI ur, Call call) throws Exception {

		try {

			Client user = Client.create();
			String route = "false";
			WebResource wr = user
					.resource(ur);

			String input = "{\"key\":\""+call.getKey()+"\",\"value\":\""+call.getValue()+"\",\"send\":\""+route+"\"}";

			ClientResponse rep = wr.type("application/json")
					.post(ClientResponse.class, input);

			if (rep.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ rep.getStatus());
			}

			String output = rep.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();
			throw e;

		}

	}
	
	public static void getReq(URI ur){
		try {

			Client user = Client.create();

			WebResource wr = user
					.resource(ur);
			
			ClientResponse rep =wr.accept("application/json")
			.get(ClientResponse.class);
			
			rep = wr.accept("application/json")
						.get(ClientResponse.class);
			

			if (rep.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ rep.getStatus());
			}

			String output = rep.getEntity(String.class);

			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();

		}
	}
	
	public static void updateReq(URI ur, Call call) throws Exception {

		try {

			Client user = Client.create();
			String redirection = "false";
			WebResource wr = user
					.resource(ur);

			String input = "{\"key\":\""+call.getKey()+"\",\"value\":\""+call.getValue()+"\",\"send\":\""+redirection+"\"}";

			ClientResponse rep = wr.type("application/json")
					.put(ClientResponse.class, input);

			if (rep.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ rep.getStatus());
			}

			System.out.println("Output from Server .... \n");
			String output = rep.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();
			throw e;

		}

	}
	
	public static void deleteReq(URI ur) throws Exception {

		try {

			Client user = Client.create();
			WebResource wr = user
					.resource(ur);

			ClientResponse rep =wr.accept("application/json").delete(ClientResponse.class);
					
					
			if (rep.getStatus() != 201) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ rep.getStatus());
			}

			String output = rep.getEntity(String.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();
			throw e;

		}

	}
	

}
