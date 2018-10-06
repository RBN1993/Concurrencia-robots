//// v1.0.1
//
//import org.jcsp.lang.*;
//import es.upm.babel.cclib.ConcIO;
//
//
//public class LogisticaCSP {
//	public static final void main(final String[] args) throws InterruptedException {
//		ControlAccesoNavesCSP can = new ControlAccesoNavesCSP();    
//
//		CSProcess[] threads = new CSProcess[Robots.N_ROBOTS+1];
//
//		for (int rid = 0; rid < Robots.N_ROBOTS; rid++) {
//			threads[rid] = new ControlRobot(rid,can);
//		}
//		threads[Robots.N_ROBOTS] = can;
//
//		Parallel sistema = new Parallel(threads);
//		sistema.run();
//	}
//}
//
//class ControlRobot implements CSProcess {
//	private int rid;
//	private ControlAccesoNaves can;
//
//	protected ControlRobot () {
//	}
//
//	public ControlRobot (int rid, ControlAccesoNaves can) {
//		this.rid = rid;
//		this.can = can;
//	}
//
//	public static int finished = 0;//lo declaro yo para ver si terminan todos los robots
//
//	public void run () {
//		int pesoAnterior;
//		int peso = Robots.PESO_VACIO;
//		for (int nid = 0; nid < Robots.N_NAVES; nid++) {
//			ConcIO.printfnl("Robot " + rid + " solicitando entrar en nave " + nid + " con peso " + peso);
//			can.solicitarEntrar(nid,peso);
//			ConcIO.printfnl("Robot " + rid + " entrando en nave " + nid);
//			Robots.entrarEnNave(rid,nid);
//			ConcIO.printfnl("Robot " + rid + " en nave " + nid);
//			ConcIO.printfnl("Robot " + rid + " cargando");
//			pesoAnterior = peso;
//			peso = Robots.cargar(rid,nid);
//			ConcIO.printfnl("Robot " + rid + " pesa " + peso);
//			ConcIO.printfnl("Robot " + rid + " solicitando salir de nave " + nid);
//			can.solicitarSalir(nid,pesoAnterior);
//			ConcIO.printfnl("Robot " + rid + " saliendo de nave " + nid);
//			Robots.entrarEnPasillo(rid,nid+1);
//			if (nid < Robots.N_NAVES - 1) {
//				ConcIO.printfnl("Robot " + rid + " en pasillo de nave " + (nid+1));
//			}
//		}
//		ConcIO.printfnl("Robot " + rid + " termina el recorrido con peso " + peso);
//		ConcIO.printfnl("Han terminado " + ++finished + " robots");//Imprimo el numero de robots que terminan
//	}
//}