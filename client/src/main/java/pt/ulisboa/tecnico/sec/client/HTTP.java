package pt.ulisboa.tecnico.sec.client;

import java.security.Key;
import org.json.JSONObject;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import pt.ulisboa.tecnico.sec.lib.http.*;
import pt.ulisboa.tecnico.sec.lib.exceptions.*;

import pt.ulisboa.tecnico.sec.exceptions.*;

public class HTTP {

	private Session session = Session.getInstance();

	public JSONObject post(String url, JSONObject json) throws Exception {

		// Put Sequence Number
		try{
			json = session.SeqNumber().request(json);
		}
		catch(NullPointerException e){ }

		// Put Digital Signature
		String reqToken = session.DigitalSignature().sign(json);

		String body = json.toString();

		HttpResponse<JsonNode> request = Unirest.post(url)
			.header("accept", "application/json")
			.header("Authorization", "Bearer "+reqToken)
			.body(body)
			.asJson();

		JSONObject response = request.getBody().getObject();

		// Verify Digital Signature
		String token = request.getHeaders().get("Authorization").get(0).split("Bearer ")[1];
		if(!session.DigitalSignature().verify(token, response))
			throw new DigitalSignatureErrorException(token);

		// Verify Sequence Number
		try{
			if(!session.SeqNumber().verify(json))
				throw new RepetedNounceException(token);
		}
		catch(NullPointerException e){ }

		return response;
	}
}
