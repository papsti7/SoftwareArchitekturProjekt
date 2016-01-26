package sa_frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 *
 * @author nikuxu
 */
public class workclass {

    public static void sendHTTPPost(String URL, JSONObject body) {
        HttpURLConnection conn = null;

        try {
            //Create connection
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);
            
            System.out.println(body.toString());

            try (OutputStream output = conn.getOutputStream()) {
                output.write(body.toString().getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            System.out.println(response.toString());

        } catch (Exception e) {
            System.out.println("ERROR, Class: workclass, Method: "
                    + "sendHTTPRequest, EX: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static JSONObject sendHTTPRequest(String URL) {
        HttpURLConnection conn = null;
        JSONObject myjson = null;

        try {
            //Create connection
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            myjson = new JSONObject(response.toString());

        } catch (Exception e) {
            System.out.println("ERROR, Class: workclass, Method: "
                    + "sendHTTPRequest, EX: " + e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return myjson;
    }

    public static DefaultTableModel createLineTableModel(JSONObject jsonob) throws JSONException {
        Object[] columnames = new Object[4];
        columnames[0] = "id";
        columnames[1] = "name";
        columnames[2] = "start name";
        columnames[3] = "end name";

        DefaultTableModel table_model = new DefaultTableModel(columnames, 0);
        table_model.setColumnCount(4);

        Iterator<?> keys = jsonob.keys();

        while (keys.hasNext()) {
            Object[] entries = new Object[4];
            String key = (String) keys.next();
            JSONArray array = jsonob.getJSONArray(key);

            entries[0] = array.getInt(0);
            entries[2] = array.getString(2);
            entries[3] = array.getString(3);

            entries[1] = key;
            table_model.addRow(entries);
        }

        return table_model;
    }

    public static DefaultTableModel createTableModel(JSONObject jsonob, int columncount) {

        Object[] columnames = new Object[4];
        columnames[0] = "id";
        columnames[1] = "name";
        columnames[2] = "lat";
        columnames[3] = "lon";

        DefaultTableModel table_model = new DefaultTableModel(columnames, 0);
        table_model.setColumnCount(columncount);

        Iterator<?> keys = jsonob.keys();

        while (keys.hasNext()) {
            table_model.addRow(convertJSONObjectToObjectArray(jsonob, (String) keys.next(), columncount));
        }

        return table_model;
    }

    private static Object[] convertJSONObjectToObjectArray(JSONObject jsonob, String key, int columncount) {

        Object[] obar = null;

        try {
            obar = new Object[columncount];
            JSONArray myarr = jsonob.getJSONArray(key);

            obar[0] = key;
            obar[1] = myarr.get(2);
            if (columncount > 2) {
                obar[2] = myarr.get(1);
                obar[3] = myarr.get(0);
            }

        } catch (Exception ex) {
            Logger.getLogger(workclass.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR, Class: workclass, Method: "
                    + "sendHTTPconvertJSONArrayToObjectArrayRequest, EX: " + ex.toString());
        }

        return obar;
    }

    public static DefaultTableModel createSpecialTableModel(JSONObject jsonob, int columncount) throws JSONException {

        Object[] columnames = new Object[3];
        columnames[0] = "id";
        columnames[1] = "name";
        columnames[2] = "add";

        DefaultTableModel table_model = new DefaultTableModel(columnames, 0);
        table_model.setColumnCount(columncount);

        Iterator<?> keys = jsonob.keys();

        while (keys.hasNext()) {
            System.out.println("HERE!");
            final Object[] entries = new Object[3];
            String key = (String) keys.next();
            JSONArray array = jsonob.getJSONArray(key);

            entries[1] = array.getString(2);

            JButton bt_add = new JButton("add");
            bt_add.setActionCommand(key);
            bt_add.addActionListener(new CustomHandler(table_model, entries));

            entries[0] = key;
            entries[2] = bt_add;

            table_model.addRow(entries);

        }

        return table_model;
    }

}
