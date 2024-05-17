package es.ujaen.ssccdd.curso2023_24.Listener;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Procesos.ClienteParticular;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import javax.jms.*;
import java.util.List;
import java.util.logging.*;

public class TextMsgListenerGestion implements MessageListener {
    private final String Consumer_Name;
    private final List<Mensaje> Lista;

    /**
     * Constructor parametrizado de la clase TextMsgListenerGestion.
     * @param Consumer_Name Nombre para comprobar el tipo de mensaje que se ha recibido.
     * @param Lista_Mensaje_Clientes Lista dónde se almacenaran los mensajes recibidos de los clientes.
     */
    public TextMsgListenerGestion(String Consumer_Name, List<Mensaje> Lista_Mensaje_Clientes) {
        this.Consumer_Name = Consumer_Name;
        this.Lista = Lista_Mensaje_Clientes;
    }

    @Override
    public void onMessage(Message message) {
        GsonUtil<Mensaje> gsonUtil = new GsonUtil<>();

        if (message instanceof TextMessage textMessage) {

            if (Consumer_Name.equalsIgnoreCase("Disponibilidad Cliente")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "SERVIDOR----> Se ha recibido un mensaje: " + Mensaje_Cliente.toString() + ". "  + RESET_COLOR );
                Lista.add(Mensaje_Cliente);

            } else if (Consumer_Name.equalsIgnoreCase("Disponibilidad Agencia")) {
                Mensaje Mensaje_Agencia = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "SERVIDOR----> Se ha recibido un mensaje: " + Mensaje_Agencia.toString() + ". "  + RESET_COLOR );
                Lista.add(Mensaje_Agencia);

            } else if (Consumer_Name.equalsIgnoreCase("Reserva")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "SERVIDOR----> Se ha recibido un mensaje: " + Mensaje_Cliente.toString() + ". "  + RESET_COLOR );
                Lista.add(Mensaje_Cliente);

            } else if (Consumer_Name.equalsIgnoreCase("Pago")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "SERVIDOR----> Se ha recibido un mensaje: " + Mensaje_Cliente.toString() + ". " + RESET_COLOR );
                Lista.add(Mensaje_Cliente);

            } else if (Consumer_Name.equalsIgnoreCase("Cancelacion")) {
                Mensaje Mensaje_Cliente = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "SERVIDOR----> Se ha recibido un mensaje: " + Mensaje_Cliente.toString() + ". " + RESET_COLOR );
                Lista.add(Mensaje_Cliente);

            } else {
                System.out.println( Consumer_Name + " Unknown message" );
            }
        }
    }

    /**
     * Método que recibe un mensaje codificado y devuelve el mensaje decodificado.
     * @param textMessage El mensaje de texto codificado que se ha recibido.
     * @param gsonUtil EL objeto GsonUtil que usaremos para decodificar el mensaje.
     * @return El mensaje que se ha recibido decodificado.
     */
    private Mensaje Recibir_Mensaje( TextMessage textMessage, GsonUtil<Mensaje> gsonUtil ) {
        Mensaje Mensaje_Cliente = null;
        try {
            Mensaje_Cliente = gsonUtil.decode( textMessage.getText(), Mensaje.class );
        } catch (JMSException ex) {
            Logger.getLogger(ClienteParticular.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Mensaje_Cliente;
    }
}