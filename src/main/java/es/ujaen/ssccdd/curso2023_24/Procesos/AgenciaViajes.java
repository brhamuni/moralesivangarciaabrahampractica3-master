package es.ujaen.ssccdd.curso2023_24.Procesos;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

import es.ujaen.ssccdd.curso2023_24.Utils.Estancia;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import es.ujaen.ssccdd.curso2023_24.Utils.Viaje;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;


public class AgenciaViajes implements Runnable{
    private final int Id;
    private final String Nombre;
    private final Semaphore Sem_Agencias_Viaje[];

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    private Destination Realizacion_Pago;
    private List<Destination> Confirmacion_Pago;
    private Destination Realizacion_Cancelacion;
    private List<Destination> Respuesta_Cancelacion;
    private Destination Realizacion_Reserva;
    private List<Destination> Confirmacion_Reserva;
    private Destination Preguntar_Disponibilidad;
    private List<Destination> Respuesta_Disponibilidad;

    /**
     * @brief Constructor parametrizado de la clase AgenciaViajes.
     * @param Id Número de identificación unico del cliente.
     * @param Num_Clientes Número de clientes asociados a la clase cliente particular.
     */
    public AgenciaViajes(int Id, int Num_Clientes, Semaphore Sem_Agencias_Viaje[]) {
        this.Id = Id;
        this.Nombre = "" + NombreAgencias.getNombre();
        this.Sem_Agencias_Viaje = Sem_Agencias_Viaje;

        Realizacion_Pago = null;
        Realizacion_Cancelacion = null;
        Realizacion_Reserva = null;
        Preguntar_Disponibilidad = null;

        Confirmacion_Pago = new ArrayList<>(Num_Clientes);
        Respuesta_Cancelacion = new ArrayList<>(Num_Clientes);
        Confirmacion_Reserva = new ArrayList<>(Num_Clientes);
        Respuesta_Disponibilidad = new ArrayList<>(Num_Clientes);

    }


    @Override
    public void run() {
        System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' comienza su EJECUCIÓN ");
        try {
            before();
            execution();
        }catch(Exception e){
            System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' ha sido INTERRUMPIDO " );
        }finally {
            after();
            System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' finaliza su EJECUCIÓN ");
        }
    }

    /**
     * @brief  Método para preparar las conexiones y sesiones antes de ejecutar las operaciones del cliente.
     * @throws Exception  Si ocurre algún error durante la configuración de las conexiones y sesiones.
     */
    public void before() throws Exception{
        connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Realizacion_Pago = session.createQueue(QUEUE+"Realizacion_Pago.Agencia");
        Realizacion_Reserva = session.createQueue(QUEUE+"Realizacion_Reserva.Agencia");
        Realizacion_Cancelacion = session.createQueue(QUEUE+"Realizacion_Cancelacion.Agencia");
        Preguntar_Disponibilidad = session.createQueue(QUEUE+"Preguntar_Disponibilidad.Agencia");

        Confirmacion_Pago.add(session.createQueue(QUEUE+"Confirmacion_Pago.Agencia"+Id));
        Respuesta_Cancelacion.add(session.createQueue(QUEUE+"Respuesta_Cancelacion.Agencia"+Id));
        Confirmacion_Reserva.add(session.createQueue(QUEUE+"Confirmacion_Reserva.Agencia"+Id));
        Respuesta_Disponibilidad.add(session.createQueue(QUEUE+"Respuesta_Disponibilidad.Agencia"+Id));

        connection.start();
    }

    public void execution() throws Exception {
        ComprobarDisponibilidad();
        TimeUnit.SECONDS.sleep(5);
        Mensaje Respuesta_Servidor = RecibirMensaje(Respuesta_Disponibilidad);
        System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' recibe los viajes disponibles ");

        RealizarReserva(Respuesta_Servidor);
        TimeUnit.SECONDS.sleep(5);
        Respuesta_Servidor = RecibirMensaje(Confirmacion_Reserva);

        if(!Respuesta_Servidor.isReserva_Correcta()){
            System.out.println( "La peticion de reserva de la agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' no se ha podido realizar");
        }else {
            RealizarPagoReserva(Respuesta_Servidor);
            TimeUnit.SECONDS.sleep(3);
            Respuesta_Servidor = RecibirMensaje(Confirmacion_Pago);
            if (Numero_Aleatorio.nextInt(100) < PROBABILIDAD_CANCELACION ) {
                CancelarReserva(Respuesta_Servidor);
                TimeUnit.SECONDS.sleep(3);
                Respuesta_Servidor = RecibirMensaje(Respuesta_Cancelacion);
                System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' tristemente y finalmente NO SE VA DE VACACIONES. ");
            }else{
                System.out.println( "Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' SE VA DE VACACIONES. ");
            }
        }
    }

    /**
     * @brief Método que envia un mensaje al servidor preguntando por la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void ComprobarDisponibilidad() throws JMSException{

        Mensaje Datos_Cliente = new Mensaje(TipoCliente.AGENCIA,TipoMensaje.CLIENTE,Nombre,Id);
        EnviarMensaje(Datos_Cliente,Preguntar_Disponibilidad);
        System.out.println("Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje solicitando las disponibilidades. ");

    }

    /**
     * @brief Método que envia un mensaje al servidor con una reserva de una estancio y\o viaje.
     * @param Respuesta Mensaje que contiene la información sobre las opciones disponibles.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarReserva(Mensaje Respuesta) throws JMSException{

        int Num_Viaje = -1, Num_Estancia = -1;
        boolean Elegir_Viaje = Numero_Aleatorio.nextBoolean();
        boolean Elegir_Estancia = Numero_Aleatorio.nextBoolean();
        boolean Cancelacion = Numero_Aleatorio.nextBoolean();
        List<Viaje> Lista_Viajes = Respuesta.getLista_Viajes_Disponibles();
        List<Estancia> Lista_Estancias = Respuesta.getLista_Estancias_Disponibles();

        if( !Elegir_Viaje && !Elegir_Estancia){
            return;
        }
        if( Elegir_Viaje && Elegir_Estancia){
            Viaje Viaje_Elegido = Lista_Viajes.get( Numero_Aleatorio.nextInt(Lista_Viajes.size()));
            Num_Viaje = Viaje_Elegido.getID();
            Estancia Estancia_Elegida = Lista_Estancias.get( Numero_Aleatorio.nextInt(Lista_Estancias.size()));
            Num_Estancia = Estancia_Elegida.getId();
        }
        if(Elegir_Viaje && !Elegir_Estancia){
            Viaje Viaje_Elegido = Lista_Viajes.get( Numero_Aleatorio.nextInt(Lista_Viajes.size()));
            Num_Viaje = Viaje_Elegido.getID();
        }
        if(!Elegir_Viaje && Elegir_Estancia){
            Estancia Estancia_Elegida = Lista_Estancias.get( Numero_Aleatorio.nextInt(Lista_Estancias.size()));
            Num_Estancia = Estancia_Elegida.getId();
        }
        Respuesta.setTipo_Mensaje(TipoMensaje.RESERVA);
        Respuesta.setCancelacion_Viaje(Cancelacion);
        Respuesta.setNum_Viaje(Num_Viaje);
        Respuesta.setNum_Estancia(Num_Estancia);

        System.out.println("Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' envia una peticion de reserva. ");
        EnviarMensaje(Respuesta, Realizacion_Reserva);

    }

    /**
     * @brief Método que realiza el pago y envia un mensaje al servidor de verificacion.
     * @param Respuesta Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarPagoReserva(Mensaje Respuesta) throws JMSException{

        double Pago_Total = 0;
        if( Respuesta.getNum_Viaje() != -1){
            Pago_Total = Respuesta.getLista_Viajes_Disponibles().get(Respuesta.getNum_Viaje()).getPrecio();
        }
        if( Respuesta.getNum_Estancia() != -1){
            Pago_Total = Pago_Total + Respuesta.getLista_Estancias_Disponibles().get(Respuesta.getNum_Estancia()).getPrecio();
        }
        if( Respuesta.isCancelacion_Viaje() ){
            Pago_Total = Pago_Total * 1.2;
        }
        Respuesta.setTipo_Mensaje(TipoMensaje.PAGAR);
        Respuesta.setPago(Pago_Total);
        EnviarMensaje(Respuesta, Realizacion_Pago);

    }

    /**
     * @brief Método para enviar un mensaje al servidor pidiendo la cancelacion de la reserva previamente hecha.
     * @param MensajeCancelacion Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void CancelarReserva(Mensaje MensajeCancelacion) throws JMSException{

        MensajeCancelacion.setTipo_Mensaje(TipoMensaje.CANCELACION);
        EnviarMensaje(MensajeCancelacion,Realizacion_Cancelacion);
        System.out.println("Agencia con nombre '" + Nombre + "' e ID '(" + Id + ")' solicita cancelar la reserva. ");

    }

    /**
     * @brief Método para codificar y enviar un mensaje al servidor de GestionViajes.
     * @param MensajeCliente Mensaje que va a ser codificado y posteriormente enviado al servidor.
     * @param Buzon Buzon por dónde se va a enviar el mensaje.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    private void EnviarMensaje( Mensaje MensajeCliente, Destination Buzon) throws JMSException{
        GsonUtil<Mensaje> gsonUtil = new GsonUtil();
        MessageProducer producer = session.createProducer(Buzon);
        TextMessage mensaje = session.createTextMessage(gsonUtil.encode(MensajeCliente,Mensaje.class));

        producer.send(mensaje);
        producer.close();
    }

    /**
     * @brief Metodo que recibe un mensaje del servidor y lo decodifica.
     * @param Buzon Buzon por donde se va a recibir el mensaje.
     * @return El mensaje recibido del servidor decodificado.
     * @throws JMSException Si ocurre algún error durante el proceso de recibir el mensaje.
     */
    private Mensaje RecibirMensaje(List<Destination> Buzon) throws JMSException{
        MessageConsumer consumer = session.createConsumer(Buzon.get(Id));
        TextMessage msg = (TextMessage) consumer.receive();
        GsonUtil<Mensaje> gsonUtil = new GsonUtil();
        Mensaje Respuesta_GestionViaje = null;
        try {
            Respuesta_GestionViaje = gsonUtil.decode(msg.getText(), Mensaje.class);
        } catch (JMSException ex) {
            Logger.getLogger(ClienteParticular.class.getName()).log(Level.SEVERE, null, ex);
        }
        consumer.close();
        return Respuesta_GestionViaje;
    }

    /**
     * @brief Método que cierra la conexión con el servidor, liberando todos los recursos asociados.
     */
    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex){}
    }
}