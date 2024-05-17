package es.ujaen.ssccdd.curso2023_24.Listener;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Procesos.ClienteParticular;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import javax.jms.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

public class TextMsgListenerAgencias implements MessageListener {
    private final String Consumer_Name;
    private final List<Mensaje> Lista;
    private final List<Semaphore> Sem_Agencia_Viajes;

    /**
     * Constructor parametrizado de la clase TextMsgListenerGestion.
     * @param Consumer_Name Nombre para comprobar el tipo de mensaje que se ha recibido.
     * @param Lista_Mensaje_Agencia Lista dónde se almacenaran los mensajes recibidos de los clientes.
     */
    public TextMsgListenerAgencias(String Consumer_Name, List<Mensaje> Lista_Mensaje_Agencia, List<Semaphore> Sem_Agencias_Viajes) {
        this.Consumer_Name = Consumer_Name;
        this.Lista = Lista_Mensaje_Agencia;
        this.Sem_Agencia_Viajes = Sem_Agencias_Viajes;
    }

    @Override
    public void onMessage(Message message) {
        GsonUtil<Mensaje> gsonUtil = new GsonUtil<>();

        if (message instanceof TextMessage textMessage) {

            if (Consumer_Name.equalsIgnoreCase("Disponibilidad")) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_AMARILLO + "AGENCIA PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". "  + RESET_COLOR);
                Lista.add(Mensaje_Servidor);
                Sem_Agencia_Viajes.get(Mensaje_Servidor.getId_Cliente()).release();
            } else if (Consumer_Name.equalsIgnoreCase("Reserva")) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_AMARILLO + "AGENCIA PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". "  + RESET_COLOR);
                Lista.add(Mensaje_Servidor);
                Sem_Agencia_Viajes.get(Mensaje_Servidor.getId_Cliente()).release();
            } else if (Consumer_Name.equalsIgnoreCase("Pago")) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_AMARILLO + "AGENCIA PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". " + RESET_COLOR);
                Lista.add(Mensaje_Servidor);
                Sem_Agencia_Viajes.get(Mensaje_Servidor.getId_Cliente()).release();
            } else if (Consumer_Name.equalsIgnoreCase("Cancelacion")) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_AMARILLO + "AGENCIA ----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". " + RESET_COLOR);
                Lista.add(Mensaje_Servidor);
                Sem_Agencia_Viajes.get(Mensaje_Servidor.getId_Cliente()).release();
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
    private Mensaje Recibir_Mensaje( TextMessage textMessage, GsonUtil<Mensaje> gsonUtil) {
        Mensaje Mensaje_Servidor = null;
        try {
            Mensaje_Servidor = gsonUtil.decode( textMessage.getText(), Mensaje.class );
        } catch (JMSException ex) {
            Logger.getLogger(ClienteParticular.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Mensaje_Servidor;
    }
}