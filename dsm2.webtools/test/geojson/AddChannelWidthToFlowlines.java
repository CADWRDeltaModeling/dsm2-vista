package geojson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import gov.ca.dsm2.input.model.Channel;
import gov.ca.dsm2.input.model.DSM2Model;
import gov.ca.dsm2.input.model.XSection;
import gov.ca.dsm2.input.model.XSectionLayer;
import gov.ca.dsm2.input.parser.Parser;

public class AddChannelWidthToFlowlines {
	public static void main(String[] args) throws Exception {
		// read in dsm2 info
		DSM2Model model = new Parser().parseModel("resources/hydro_2009calibration_navd88.inp").toDSM2Model();
		// read flow line information
		FileReader reader = new FileReader(new File("web/json/dsm2-flowlines-oct312016.geojson"));
		JsonParser parser = new JsonParser();
		JsonElement parse = parser.parse(reader);
		JsonArray featureArray = parse.getAsJsonObject().getAsJsonArray("features");
		System.out.println("# of features: "+featureArray.size());
		HashMap<String, JsonElement> idToFeatureMap = new HashMap<String, JsonElement>();
		for (int i = 0; i < featureArray.size(); i++) {
			JsonObject properties = featureArray.get(i).getAsJsonObject().getAsJsonObject("properties");
			String id = properties.getAsJsonPrimitive("channel_nu").getAsString();
			idToFeatureMap.put(id, featureArray.get(i));
			System.out.println(id);
			// channel-width property mixed from channels (use average top width from all xsections)
			properties.add("width", new JsonPrimitive(
					getTopWidth(model.getChannels().getChannel(id))));
		}
		reader.close();
		List<Integer> sortedKeys = new ArrayList<Integer>(idToFeatureMap.size());
		for (String key: idToFeatureMap.keySet()){
			sortedKeys.add(new Integer(key));
		}
		Collections.sort(sortedKeys);
		parse.getAsJsonObject().remove("features");
		featureArray = new JsonArray();
		for(Integer key: sortedKeys){
			featureArray.add(idToFeatureMap.get(key.toString()));
		}
		parse.getAsJsonObject().add("features", featureArray);
		
		// write it out to json
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Writer writer = new FileWriter("web/json/dsm2-flowlines-width-oct312016.js");
		writer.write("dsm2_flowlines=");
		gson.toJson(parse, writer);
		writer.close();
	}

	private static Number getTopWidth(Channel channel) {
		float topWidths = 0;
		int nWidths = 0;
		for (XSection xs : channel.getXsections()) {
			for (XSectionLayer layer : xs.getLayers()) {
				topWidths += layer.getTopWidth();
				nWidths++;
			}
		}
		if (nWidths == 0)
			return new Float(0);
		return new Float(topWidths / nWidths);
	}
}
