package es.ujaen.ssccdd.curso2023_24.Listener;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Procesos.ClienteParticular;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import javax.jms.*;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

public class TextMsgListenerClientes implements MessageListener {
    private final String Consumer_Name;
    private final List<Mensaje> Lista;
    private final Semaphore Sem_Cliente_Particular;

    /**
     * Constructor parametrizado de la clase TextMsgListenerGestion.
     * @param Consumer_Name Nombre para comprobar el tipo de mensaje que se ha recibido.
     * @param Lista_Mensaje_Clientes Lista dónde se almacenaran los mensajes recibidos de los clientes.
     * @param Sem_Cliente_Particular Semaforo para desbloquear al cliente una vez que recibe un mensaje.
     */
    public TextMsgListenerClientes( String Consumer_Name, List<Mensaje> Lista_Mensaje_Clientes, Semaphore Sem_Cliente_Particular ) {
        this.Consumer_Name = Consumer_Name;
        this.Lista = Lista_Mensaje_Clientes;
        this.Sem_Cliente_Particular = Sem_Cliente_Particular;
    }

    @Override
    public void onMessage(Message message) {
        GsonUtil<Mensaje> gsonUtil = new GsonUtil<>();

        if (message instanceof TextMessage textMessage) {

            if ( Consumer_Name.equalsIgnoreCase("Disponibilidad") ) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "CLIENTE PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". "  + RESET_COLOR );
                Lista.add(Mensaje_Servidor);
                Sem_Cliente_Particular.release();

            } else if ( Consumer_Name.equalsIgnoreCase("Reserva") ) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "CLIENTE PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". "  + RESET_COLOR );
                Lista.add(Mensaje_Servidor);
                Sem_Cliente_Particular.release();

            } else if ( Consumer_Name.equalsIgnoreCase("Pago") ) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "CLIENTE PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". " + RESET_COLOR );
                Lista.add(Mensaje_Servidor);
                Sem_Cliente_Particular.release();

            } else if ( Consumer_Name.equalsIgnoreCase("Cancelacion") ) {
                Mensaje Mensaje_Servidor = Recibir_Mensaje( textMessage, gsonUtil );
                System.out.println( TEXTO_MORADO + "CLIENTE PARTICULAR----> Se ha recibido un mensaje: " + Mensaje_Servidor.toString() + ". " + RESET_COLOR );
                Lista.add(Mensaje_Servidor);
                Sem_Cliente_Particular.release();

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
        Mensaje Mensaje_Servidor = null;
        try {
            Mensaje_Servidor = gsonUtil.decode( textMessage.getText(), Mensaje.class );
        } catch (JMSException ex) {
            Logger.getLogger(ClienteParticular.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Mensaje_Servidor;
    }
}