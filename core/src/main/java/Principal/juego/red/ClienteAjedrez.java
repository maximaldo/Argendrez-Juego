package Principal.juego.red;

import Principal.juego.elementos.ColorPieza;

import java.io.IOException;
import java.net.*;
import java.util.Collections;

/**
 * Cliente UDP para el ajedrez.
 * Sigue los mismos principios que HiloCliente del ejercicio de chat:
 *  - Descubre el servidor por broadcast ("Hello_there")
 *  - Usa DatagramSocket y DatagramPacket
 *  - Se conecta enviando "Conectar"
 *  - Se comunica siempre por mensajes de texto.
 */
public class ClienteAjedrez extends Thread {

    public interface ReceptorMensajes {
        void onColorAsignado(ColorPieza color);
        void onMovimientoRecibido(int sx, int sy, int dx, int dy);
        void onConexionEstablecida();
    }

    private final DatagramSocket socket;
    private final InetAddress ipServer;
    private final int puertoServidor;
    private final ReceptorMensajes receptor;

    private volatile boolean fin = false;
    public volatile boolean conexionEstablecida = false;

    public ClienteAjedrez(ReceptorMensajes receptor) throws IOException {
        this.receptor = receptor;
        try {
            this.socket = new DatagramSocket();
            this.socket.setBroadcast(true);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        InetSocketAddress servidor = descubrirServidor();
        this.ipServer = servidor.getAddress();
        this.puertoServidor = servidor.getPort();

        enviarMensajePlano("Conectar");
        System.out.println("[CLIENTE] Conectando con servidor en " + ipServer + ":" + puertoServidor);
    }

    @Override
    public void run() {
        while (!fin) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagrama);
                procesarMensaje(datagrama);
            } catch (IOException e) {
                if (!fin) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void procesarMensaje(DatagramPacket datagrama) {
        String mensaje = new String(
            datagrama.getData(),
            0,
            datagrama.getLength()
        ).trim();

        System.out.println("[CLIENTE] Recibido: " + mensaje);

        if (mensaje.equals("Conexion establecida")) {
            conexionEstablecida = true;
            if (receptor != null) receptor.onConexionEstablecida();
            return;
        }

        if (mensaje.startsWith("COLOR:")) {
            String col = mensaje.substring("COLOR:".length()).trim();
            ColorPieza c = col.equalsIgnoreCase("BLANCO")
                ? ColorPieza.BLANCO
                : ColorPieza.NEGRO;
            if (receptor != null) receptor.onColorAsignado(c);
            return;
        }

        if (mensaje.startsWith("MOVE:")) {
            String coords = mensaje.substring("MOVE:".length());
            String[] partes = coords.split(",");
            if (partes.length == 4) {
                try {
                    int sx = Integer.parseInt(partes[0]);
                    int sy = Integer.parseInt(partes[1]);
                    int dx = Integer.parseInt(partes[2]);
                    int dy = Integer.parseInt(partes[3]);
                    if (receptor != null) receptor.onMovimientoRecibido(sx, sy, dx, dy);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // === API pública para el juego ===

    public void enviarMovimiento(int sx, int sy, int dx, int dy) {
        String msg = "MOVE:" + sx + "," + sy + "," + dx + "," + dy;
        enviarMensajePlano(msg);
    }

    private void enviarMensajePlano(String mensaje) {
        byte[] buffer = mensaje.getBytes();
        DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, ipServer, puertoServidor);
        try {
            socket.send(datagrama);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void cerrar() {
        fin = true;
        socket.close();
    }

    // === Descubrimiento de servidor (mismos principios que el TP de chat) ===

    private InetSocketAddress descubrirServidor() throws IOException {

        byte[] data = "Hello_there".getBytes();
        InetAddress broadcast = obtenerBroadcast();

        DatagramPacket broadcastDatagram = new DatagramPacket(
            data,
            data.length,
            broadcast,
            4321
        );

        socket.send(broadcastDatagram);
        System.out.println("[CLIENTE] Broadcast enviado a " + broadcast.getHostAddress());

        // esperar respuesta
        byte[] buffer = new byte[1024];
        DatagramPacket respuesta = new DatagramPacket(buffer, buffer.length);
        socket.receive(respuesta);

        System.out.println("[CLIENTE] Respuesta de discovery: " +
            respuesta.getAddress() + ":" + respuesta.getPort());

        return new InetSocketAddress(
            respuesta.getAddress(),
            respuesta.getPort()
        );
    }

    private InetAddress obtenerBroadcast() throws SocketException {
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || ni.isLoopback()) continue;

            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                InetAddress broadcast = ia.getBroadcast();
                if (broadcast != null) return broadcast;
            }
        }
        throw new RuntimeException("No se encontró broadcast");
    }
}
