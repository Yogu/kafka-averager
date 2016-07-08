package de.unistuttgart.isw.serviceorchestration;

import com.siemens.ct.exi.exceptions.EXIException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.unistuttgart.isw.serviceorchestration.servicecore.MessageBus;
import de.unistuttgart.isw.serviceorchestration.servicecore.MessageSender;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AveragerMain {

    private static float value;
    private static List<Float> valueList;
    private static float average;

    public static void main(String[] args) throws IOException, EXIException, SAXException, InterruptedException {
        // opcfoundation does not like Java default user agent
        System.setProperty("http.agent", "Mozilla/5.0");
        valueList = new ArrayList<>();


        MessageBus bus = new MessageBus();
        MessageSender sender = bus.createSender("output", "https://opcfoundation.org/UA/2008/02/Types.xsd");

        bus.createReceiver("input", "https://opcfoundation.org/UA/2008/02/Types.xsd",
                xml -> {
                    XStream xstream = new XStream(new StaxDriver());
                    xstream.alias("Float", Float.class);

                    Float value = (Float) xstream.fromXML(xml);

                    valueList.add(value);

                    if (valueList.size() == 10) {
                        average = (float) valueList.stream().mapToDouble(i -> i).average().getAsDouble();
                        valueList.clear();
                        try {
                            sender.send(xstream.toXML(average));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                });

        bus.runListener();

    }


}
