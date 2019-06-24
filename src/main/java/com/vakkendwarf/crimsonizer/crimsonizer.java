package com.vakkendwarf.crimsonizer;

import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
import com.github.koraktor.steamcondenser.steam.servers.*;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;


public class crimsonizer {

    private static final String APPLICATION_NAME = "CrimsonReader";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        System.out.println("Preparing credentials...");

        // Load client secrets.
        InputStream in = crimsonizer.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        System.out.println("GOT CRED FILE");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        System.out.println("CRED FILE UPLOADED");

        System.out.println("Client secrets loaded.");

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

    }

    public static void UpdateMySpreadhseet(Object value, Sheets service) throws IOException {

        final String spreadsheetId = "1Go-ZRsztYsVHuOudp1agIy3b9n0lHwdfGcVtnxuIMc8";

        /* RANGE */

        LocalDate date = new LocalDate();

        LocalDate firstday = new LocalDate(2019,6,2);

        int dayssince = Math.abs(Days.daysBetween(date, firstday).getDays());

        System.out.println("DAYS SINCE START OF SPREADHSEET: " + dayssince);

        String rangex = Integer.toString(2 + dayssince);

        int hours = new LocalTime().getHourOfDay() - 10;

        if ( (hours + 10)>23 || (hours+10) < 10 )
            return;

        char[] alphabet = {'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'};

        String rangey = alphabet[hours] + rangex;

        System.out.println("CURRENT CELL IS: " + rangey);

        /* END RANGE */

        Object val = (Integer) value;

        List<List<Object>> values = Arrays.asList(Arrays.asList(
                val
        ));

        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, rangey + ":" + rangey, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());

    }

    public static void Crimsonizer() throws IOException, GeneralSecurityException {

        System.out.println("Crimsonizer has been activated.");

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        System.out.println("Setting up HTTP_Transport complete.");

        System.out.println(getCredentials(HTTP_TRANSPORT).toString());

        System.out.println("Successfully got credentials.");

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        System.out.println("Sheets service activated and built.");

        byte[] ipAddr = new byte[]{(byte)145, (byte)239, (byte)18, (byte)137};

        System.out.print("IP HAS BEEN ASSEMBLED: ");
        System.out.println(ipAddr.toString());

        try {
            InetAddress serverIp = InetAddress.getByAddress(ipAddr);

            System.out.print("IP HAS BEEN ASSIGNED: ");
            System.out.print(ipAddr.toString());
            System.out.println("");

            try {
                SourceServer server = new SourceServer(serverIp, 1704) {
                };
                try {

                    server.initialize();
                    HashMap<String, Object> serverinfo = new HashMap<String, Object>();
                    serverinfo = server.getServerInfo();
                    System.out.println("CURRENT CRIMSON GAMING SWRP PLAYERCOUNT: " + serverinfo.get("numberOfPlayers"));

                    UpdateMySpreadhseet(serverinfo.get("numberOfPlayers"), service);

                }
                catch (SteamCondenserException e) {
                    System.out.println("Could not initialize the server.");
                }
                catch (TimeoutException e) {
                    System.out.println("Server timed out.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SteamCondenserException e) {
                System.out.println("Server object could not be created.");
            }

        }
        catch (java.net.UnknownHostException e) {
            System.out.println("Server not found");
        }

    }

    public static void main(String[] args) {

        System.out.println("Initializing Crimsonizer...");

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

        System.out.println("Starting SES...");

        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Crimsonizer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1, TimeUnit.HOURS);

        System.out.println("SES Started.");

    }

}
