package es.ujaen.ssccdd.curso2023_24.Listener;

import es.ujaen.ssccdd.curso2023_24.Procesos.ClienteParticular;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import javax.jms.*;
import java.util.List;
import java.util.logging.*;

public class TextMsgListenerGestion implements MessageListener {
    private String Consumer_Name;
    private List<Mensaje> Lista;

    public TextMsgListenerGestion(String Consumer_Name, List<Mensaje> Lista_Mensaje_Clientes) {
        this.Consumer_Name = Consumer_Name;
        this.Lista = Lista_Mensaje_Clientes;
    }

    @Override
    public void onMessage(Message message) {
        GsonUtil<Mensaje> gsonUtil = new GsonUtil();

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            if (Consumer_Name.equalsIgnoreCase("Disponibilidad Cliente")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje(textMessage,gsonUtil);
                System.out.println("Se ha recibido una solicitud de Disponibilidad del (CLIENTE " + Mensaje_Cliente.getNombre_Cliente() + ")");
                Lista.add(Mensaje_Cliente);

            } else if (Consumer_Name.equalsIgnoreCase("Disponibilidad Agencia")) {
                Mensaje Mensaje_Agencia = Recibir_Mensaje(textMessage,gsonUtil);
                System.out.println("Se ha recibido una solicitud de Disponibilidad de la (AGENCIA " + Mensaje_Agencia.getNombre_Cliente() + ")");
                Lista.add(Mensaje_Agencia);
            } else if (Consumer_Name.equalsIgnoreCase("Reserva")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje(textMessage,gsonUtil);
                System.out.println("Se ha recibido una solicitud de reserva de ( " + Mensaje_Cliente.getTipo_Cliente() + ": " + Mensaje_Cliente.getNombre_Cliente() +" "+ Mensaje_Cliente.getId_Cliente() +" )");
                Lista.add(Mensaje_Cliente);
            } else if (Consumer_Name.equalsIgnoreCase("Pago")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje(textMessage,gsonUtil);
                System.out.println("Se ha recibido una solicitud de pago de ( " + Mensaje_Cliente.getTipo_Cliente() + ": " + Mensaje_Cliente.getNombre_Cliente() + " )");
                Lista.add(Mensaje_Cliente);
            } else if (Consumer_Name.equalsIgnoreCase("Cancelacion")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje(textMessage,gsonUtil);
                System.out.println("Se ha recibido una solicitud de cancelacion de ( " + Mensaje_Cliente.getTipo_Cliente() + ": " + Mensaje_Cliente.getNombre_Cliente() + " )");
                Lista.add(Mensaje_Cliente);
            } else {
                System.out.println(Consumer_Name + " Unknown message");
            }
        }
    }

    /**
     * @brief Método que recibe un mensaje codificado y devuelve el mensaje decodificado.
     * @param textMessage El mensaje de texto codificado que se ha recibido.
     * @param gsonUtil EL objeto GsonUtil que usaremos para decodificar el mensaje.
     * @return El mensaje que se ha recibido decodificado.
     */
    private Mensaje Recibir_Mensaje( TextMessage textMessage, GsonUtil<Mensaje> gsonUtil) {
        Mensaje Mensaje_Cliente = null;
        try {
            Mensaje_Cliente = gsonUtil.decode(textMessage.getText(), Mensaje.class);
        } catch (JMSException ex) {
            Logger.getLogger(ClienteParticular.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Mensaje_Cliente;
    }
}