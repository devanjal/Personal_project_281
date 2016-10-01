package com.dev.pro;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.poi.hssf.record.aggregates.RecordAggregate.PositionTrackingVisitor;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.personal.pro.SMVersion2ImplHandler;
import com.dev.obj.Call;
import com.dev.obj.Reply;
import com.dev.jul.RestOperation;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path("/dsk")
public class RestFinal {
	
	@Context
	UriInfo uriInfo;
	
	private static String myIP = "172.32.2.6";
	private static String[] slaveIPs =  {"172.32.0.98","172.32.2.6"};
	private static String masterIP = "localhost";
	
	
	
	
	@POST
	@Path("/store")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertDataInDS(Call call) {
		
		Reply rep = new Reply();
		JSONObject jsonOb = new JSONObject();
		URI uricheck = uriInfo.getAbsolutePath();
		
		if(!isReachable(masterIP)){
				
				rep.setUniqueAddress(uricheck.getHost());
				rep.setSuccess("System is running in partition mode now.");
				return Response.status(201).entity(rep).build();
			
		}
		
		
		boolean getVal = false;
		int status= 201;
		
		
		
		if("true".equalsIgnoreCase(call.getSend())){
			if(isMaster){
				
				
				getVal = SMVersion2ImplHandler.storeD1( call);
				
				rep.setUniqueAddress(masterIP);
				rep.setSuccess(""+getVal);
				
				for(int i=0; 1<slaveIPs.length-1; i++){
					String ip = slaveIPs[i];
					UriBuilder builder= UriBuilder.fromUri(uriInfo.getAbsolutePath()).host(ip).port(8080);
					URI newURI  = builder.build();
						
					try {
						if(isReachable(ip)){
							RestOperation.sendReq(newURI,call);
						}
						else{
							System.out.println("Slave down"+ip);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				 
			}
			else{
				
				UriBuilder builder;
				URI newURI;
					
					builder = UriBuilder.fromUri(uriInfo.getAbsolutePath()).host(masterIP).port(8080);
					newURI = builder.build();
					
					try {
	
						Client client = Client.create();
	
						WebResource webResource = client
								.resource(newURI);
	
						String input = "{\"key\":\""+call.getKey()+"\",\"value\":\""+call.getValue()+"\",\"send\":\""+call.getSend()+"\"}";
						System.out.println("Redirected to Master ======>");
						ClientResponse response1 = webResource.type("application/json")
								.post(ClientResponse.class, input);
	
						if (response1.getStatus() != 201) {
							throw new RuntimeException("Failed  : "+ response1.getStatus());
									}
						status = response1.getStatus();
						ObjectMapper mapper = new ObjectMapper();
						rep = mapper.readValue(response1.getEntity(String.class), Reply.class);
						
					
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
				
			}
		}else {
			
			getVal = SMVersion2ImplHandler.storeD1( call);
			
			rep = new Reply();
			rep.setUniqueAddress(uricheck.getHost());
			rep.setSuccess(""+getVal);
			
		}
			
		return Response.status(201).entity(rep).build();
		
	}
	
	

	public static boolean isReachable(String host){
		 boolean available = false;
		 //If connection to socket
		      SocketAddress sockaddr = new InetSocketAddress(host, 8080);
		      //Trying to connect
                Socket socket = new Socket();
                try {
                	//if connects then set true
                    socket.connect(sockaddr, 4000);
                    available = true;
                }
                catch (IOException IOException) {
                }
        
		return available;
	}
	
	@GET
	@Path("/get/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getKey(@PathParam("key") String key) {
		
		JSONObject jsonOb = new JSONObject();
		//same for all the cases.
		
		String response = SMVersion2ImplHandler.getD2(key);
		
		//TODO partiotion case
		if( response.isEmpty()){
			try {
				jsonOb.put("Error", "No matches found");
				response = jsonOb.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	
		return response;
	}
	
	private static boolean isMaster = true;	
	
	@DELETE
	@Path("/remove/{key}/{route}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteRecord(@PathParam("key") String key, @PathParam("route") String route){
		URI uricheck = uriInfo.getAbsolutePath();
		Reply call = new Reply();
		call.setUniqueAddress(myIP);
		
		boolean resString = false;
		int status= 201;
		
		if("true".equals(route)){
			
			if(isMaster){
				boolean response = SMVersion2ImplHandler.deleteR1(key);
				
				call = new Reply();
				
				call.setUniqueAddress(uriInfo.getAbsolutePath().getHost());
				call.setSuccess(""+response);
				
				for(int i=0; 1<slaveIPs.length-1; i++){
					String ip = slaveIPs[i];
				UriBuilder builder= UriBuilder.fromUri("http://localhost:8080/connect/dk/dsk/remove/"+key+"/false").host(ip).port(8080);
					URI newURI  = builder.build();
						
					try {
						if(isReachable(ip)){
							RestOperation.deleteReq(newURI);
							}
						else {
							System.out.println("Slave down : "+ip);
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				 
			}
			else{
				UriBuilder builder;
				URI newURI;
					
					builder = UriBuilder.fromUri(uriInfo.getAbsolutePath()).host(masterIP).port(8080);
					newURI = builder.build();
					
					try {
	
						Client client = Client.create();
	
						WebResource webResource = client.resource(newURI);
						System.out.println("Routed to master:\n\n");
						ClientResponse response = webResource.accept("application/json")
											.delete(ClientResponse.class);
	
						if (response.getStatus() != 201) {
							throw new RuntimeException("Failed : "
									+ response.getStatus());
						}
						status = response.getStatus();
						ObjectMapper mapper = new ObjectMapper();
						call = mapper.readValue(response.getEntity(String.class), Reply.class);
						

	
					} catch (Exception e) {
							e.printStackTrace();
	
					}
				
			}
		}else {
			
			boolean response = SMVersion2ImplHandler.deleteR1(key);
			
			call = new Reply();
			
			call.setUniqueAddress(uriInfo.getAbsolutePath().getHost());
			call.setSuccess(""+response);
			
			/*
			call = new Reply();
			call.setUniqueAddress(myIP);
			call.setSuccess(""+response);*/
			
			
		}
		System.out.println("Deleted\n");
		return Response.status(201).entity(call).build();
	
	
	}
	
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateDataInDS(Call tranzReqDTO) {
		
		URI uricheck = uriInfo.getAbsolutePath();
		Reply tranzResDTO = new Reply();
		tranzResDTO.setUniqueAddress(myIP);
		
		
		boolean getVal = false;
		int status= 201;
	
		
		if("true".equalsIgnoreCase(tranzReqDTO.getSend())){
			if(isMaster){	
				boolean isUpdated = SMVersion2ImplHandler.updateRecord(tranzReqDTO);
				tranzResDTO = new Reply();
				
				tranzResDTO.setUniqueAddress(masterIP);
				tranzResDTO.setSuccess(""+isUpdated);
				
				for(int i=0; 1<slaveIPs.length-1; i++){
					String ip = slaveIPs[i];
					UriBuilder builder= UriBuilder.fromUri(uriInfo.getAbsolutePath()).host(ip).port(8080);
					URI newURI  = builder.build();
						
					try {
						if(isReachable(ip)){
						RestOperation.updateReq(newURI,tranzReqDTO);
						} else{
							System.out.println("Running in partiion mode "+ ip);
						}
						
					} catch (Exception e) {
						
						e.printStackTrace();
					}
					
				}
				 
			}
			else{
				
				UriBuilder builder = UriBuilder.fromUri(uriInfo.getAbsolutePath()).host(masterIP).port(8080);
				URI newURI = builder.build();
					
					try {
	
						Client client = Client.create();
	
						WebResource webResource = client
								.resource(newURI);
	
						String input = "{\"key\":\""+tranzReqDTO.getKey()+"\",\"value\":\""+tranzReqDTO.getValue()+"\",\"send\":\""+tranzReqDTO.getSend()+"\"}";
	
						ClientResponse response1 = webResource.type("application/json")
								.put(ClientResponse.class, input);
	
						if (response1.getStatus() != 201) {
							throw new RuntimeException("Failed : HTTP error code : "
									+ response1.getStatus());
						}
						/*
						 * if (response1.getStatus() != 404) {
							throw new RuntimeException("Failed : HTTP error code : "
									+ response1.getStatus());
						}
						 */
						status = response1.getStatus();
						ObjectMapper mapper = new ObjectMapper();
						tranzResDTO = mapper.readValue(response1.getEntity(String.class), Reply.class);
						
						
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
				
			}
		}else {
			
			boolean isUpdated = SMVersion2ImplHandler.updateRecord(tranzReqDTO);
			
			tranzResDTO = new Reply();

			tranzResDTO.setUniqueAddress(uricheck.getHost());
			tranzResDTO.setSuccess(""+isUpdated);
			
		}
		System.out.println("Slave updated.");
		
		return Response.status(201).entity(tranzResDTO).build();
		
	}
	
	
		 


	
}
