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


public class ClienteParticular implements Runnable {
    private final int Id;
    private final String Nombre;
    private final Semaphore Sem_Clientes_Particulares[];

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    private Destination Realizacion_Pago;
    private Destination[] Confirmacion_Pago;
    private Destination Realizacion_Cancelacion;
    private Destination[] Respuesta_Cancelacion;
    private Destination Realizacion_Reserva;
    private Destination[] Confirmacion_Reserva;
    private Destination Preguntar_Disponibilidad;
    private Destination[] Respuesta_Disponibilidad;

    /**
     * @brief Constructor parametrizado de la clase ClienteParticular.
     * @param Id Número de identificación unico del cliente.
     * @param Num_Clientes Número de clientes asociados a la clase cliente particular.
     */
    public ClienteParticular(int Id, int Num_Clientes, Semaphore Sem_Clientes_Particulares[]) {
        this.Id = Id;
        this.Nombre = "" + NombreUsuarios.getNombre();
        this.Sem_Clientes_Particulares = Sem_Clientes_Particulares;

        Realizacion_Pago = null;
        Realizacion_Cancelacion = null;
        Realizacion_Reserva = null;
        Preguntar_Disponibilidad = null;

        Confirmacion_Pago = new Destination[Num_Clientes];
        Respuesta_Cancelacion = new Destination[Num_Clientes];
        Confirmacion_Reserva = new Destination[Num_Clientes];
        Respuesta_Disponibilidad = new Destination[Num_Clientes];
    }


    @Override
    public void run() {
        System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' comienza su EJECUCIÓN ");
        try {
            before();
            execution();
        }catch(Exception e){
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' ha sido INTERRUMPIDO " );
        }finally {
            after();
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' finaliza su EJECUCIÓN ");
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

        Preguntar_Disponibilidad = session.createQueue(QUEUE+"Preguntar_Disponibilidad.Cliente");
        Realizacion_Reserva = session.createQueue(QUEUE+"Realizacion_Reserva.Cliente");
        Realizacion_Pago = session.createQueue(QUEUE+"Realizacion_Pago.Cliente");
        Realizacion_Cancelacion = session.createQueue(QUEUE+"Realizacion_Cancelacion.Cliente");

        Respuesta_Disponibilidad[Id] = session.createQueue(QUEUE+"Respuesta_Disponibilidad.Cliente"+Id);
        Confirmacion_Reserva[Id] = session.createQueue(QUEUE+"Confirmacion_Reserva.Cliente"+Id);
        Confirmacion_Pago[Id] = session.createQueue(QUEUE+"Confirmacion_Pago.Cliente"+Id);
        Respuesta_Cancelacion[Id] = session.createQueue(QUEUE+"Respuesta_Cancelacion.Cliente"+Id);

        connection.start();
    }


    public void execution() throws Exception {

        ComprobarDisponibilidad();
        Mensaje Respuesta_Servidor = RecibirMensaje(Respuesta_Disponibilidad);
        System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' recibe los viajes disponibles ");

        RealizarReserva(Respuesta_Servidor);
        Respuesta_Servidor = RecibirMensaje(Confirmacion_Reserva);
        System.out.println("El valor de la Reserva Correcta es " + Respuesta_Servidor.isReserva_Correcta());
        if(!Respuesta_Servidor.isReserva_Correcta()){
            System.out.println( "La peticion de reserva del cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' no se ha podido realizar");
        }else {
            RealizarPagoReserva(Respuesta_Servidor);
            Respuesta_Servidor = RecibirMensaje(Confirmacion_Pago);

            if (Numero_Aleatorio.nextInt(100) < PROBABILIDAD_CANCELACION ) {
                CancelarReserva(Respuesta_Servidor);
                Respuesta_Servidor = RecibirMensaje(Respuesta_Cancelacion);
                System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' finalmente no se va de vacaciones. ");
            }else{
                System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' se va de vacaciones. ");
            }
        }
    }

    /**
     * @brief Método que envia un mensaje al servidor preguntando por la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void ComprobarDisponibilidad() throws JMSException{
        Mensaje Datos_Cliente = new Mensaje(TipoCliente.PARTICULAR,TipoMensaje.CLIENTE, Nombre, Id);
        System.out.println("El nombre que contiene el mensaje es: " + Datos_Cliente.getNombre_Cliente());
        EnviarMensaje(Datos_Cliente,Preguntar_Disponibilidad);
        System.out.println("Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje solicitando las disponibilidades. ");
    }

    /**
     * @brief Método que envia un mensaje al servidor con una reserva de una estancio y\o viaje.
     * @param Respuesta Mensaje que contiene la información sobre las opciones disponibles.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarReserva(Mensaje Respuesta) throws JMSException{

        int Num_Viaje = -1, Num_Estancia = -1;
        boolean Elegir_Estancia = false;
        boolean Elegir_Viaje = false;
        boolean Cancelacion = false;
        if (Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_VIAJE){
            Elegir_Viaje=true;
        }
        if (Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_ESTANCIA){
            Elegir_Estancia=true;
        }
        if (Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_CANCELACION){
            Cancelacion=true;
        }
        List<Viaje> Lista_Viajes = Respuesta.getLista_Viajes_Disponibles();
        List<Estancia> Lista_Estancias = Respuesta.getLista_Estancias_Disponibles();

        if( Elegir_Viaje && Elegir_Estancia ){
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


        System.out.println("El nombre que contiene el mensaje es: " + Respuesta.getNombre_Cliente());
        System.out.println("Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia una peticion de reserva. ");
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
            Pago_Total = Pago_Total * PENALIZACION_POR_CANCELACION;
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
        System.out.println("Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' solicita cancelar la reserva. ");
        EnviarMensaje(MensajeCancelacion,Realizacion_Cancelacion);

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
    private Mensaje RecibirMensaje(Destination[] Buzon) throws JMSException,InterruptedException{
        Sem_Clientes_Particulares[Id].acquire();
        TimeUnit.SECONDS.sleep(1);

        MessageConsumer consumer = session.createConsumer(Buzon[Id]);
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