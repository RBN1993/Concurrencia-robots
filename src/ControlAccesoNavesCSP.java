

/*
-----------AUTOR:ROBINSON GANCHALA BENITEZ 110377, 54267793Y-----------------
 */

import org.jcsp.lang.*;
import java.util.PriorityQueue;

class ControlAccesoNavesCSP 
implements ControlAccesoNaves, CSProcess {

	public static final int N_ROBOTS = Robots.N_ROBOTS;
	public static final int N_NAVES = Robots.N_NAVES;
	public static final int MAX_PESO_NAVE = Robots.MAX_PESO_EN_NAVE;

	// DECLARACION DE CANALES PARA COMUNICAR LAS OPERACIONES DEL
	// RECURSO COMPARTIDO CON EL SERVIDOR
	// Solución mixta: uno para solicitarEntrar
	private Any2OneChannel cPetEntrar; 
	// por el que se envian objetos PetEntrar (ver mas abajo),
	// ...y tantos como naves para solicitarSalir:
	private Any2OneChannel[] cPetSalir;

	public ControlAccesoNavesCSP() {
		// construimos los canales
		cPetEntrar = Channel.any2one();
		cPetSalir = new Any2OneChannel[N_NAVES];
		for (int n = 0; n < N_NAVES; n++) {
			cPetSalir[n] = Channel.any2one();
		}
	}

	// CLASES INTERNAS PARA PETICIONES
	private class PetEntrar implements Comparable {
		public int nave;
		public int peso;
		public One2OneChannel cresp;

		PetEntrar(int n, int p) {
			this.nave = n;
			this.peso = p;
			this.cresp = Channel.one2one();
		}

		//@Override
		public int compareTo(Object otra) {
			return this.peso - ((PetEntrar)otra).peso;
		}
	}

	// CODIGO DE LLAMADA A LAS _OPERACIONES_ DEL RECURSO COMPARTIDO QUE
	// SE EJECUTA _EN_LOS_THREADS_CLIENTE_ !!
	public void solicitarEntrar(int n, int p) {
		// creamos una peticion de entrar:
		PetEntrar pet = new PetEntrar(n,p);
		// la enviamos al servidor
		cPetEntrar.out().write(pet);
		// y esperamos una confirmación
		pet.cresp.in().read();
	}
	public void solicitarSalir(int n, int p) {
		// enviamos el peso por cPetSalir[n]
		cPetSalir[n].out().write(p);
		// esto bloquea hasta que el pasillo n+1 (si existe) está libre
	}

	// CODIGO DEL _SERVIDOR_ EN EL METODO run() !!
	public void run() {
		// ESTADO DEL RECURSO COMPARTIDO AQUI
		int peso[] = new int[N_NAVES];
		boolean ocupado[] = new boolean[N_NAVES];

		// ESTADO ADICIONAL PARA PETICIONES APLAZADAS, ETC.
		// Peticiones de entrar a la nave 0:
		PriorityQueue<PetEntrar> entrar0 = new PriorityQueue<PetEntrar>();
		// Para guardar las peticiones de entrar en una nave que no es
		// la 0 basta con una por pasillo:
		PetEntrar[] entrarN = new PetEntrar[N_NAVES];

		// DECLARACION DE ESTRUCTURAS PARA RECEPCION ALTERNATIVA
		AltingChannelInput[] inputs = new AltingChannelInput[N_NAVES+1];
		// las N_NAVES primeras entradas en alternativa serán las
		// peticiones de salir:
		for (int n = 0; n < N_NAVES; n++) {
			inputs[n] = cPetSalir[n].in();
		}
		// la última entrada es para las peticiones de entrar:
		inputs[N_NAVES] = cPetEntrar.in();
		// creamos la recepción alternativa:
		Alternative servicios = new Alternative(inputs);
		// creamos el vector de booleanos para la recepción
		// condicional: 
		boolean[] sincCond = new boolean[N_NAVES+1];
		// inicialmente se puede enviar cualquier peticion,
		// tanto de entrar...
		sincCond[N_NAVES] = true;
		// ...como de salir:
		for (int n = 0; n < N_NAVES; n++) {
			sincCond[n] = true; // == !ocupado[n+1]
		}

		// VARIABLES AUXILIARES A LA RECEPCION ALTERNATIVA
		// para guardar el indice del vector de servicios:
		int cual;

		// BUCLE PRINCIPAL DEL SERVIDOR
		while (true) {
			// LA SELECT
			cual = servicios.fairSelect(sincCond);
			// tratamiento de casos...
			// peticiones de entrar:
			if (cual == N_NAVES){
				// leemos la peticion:
				PetEntrar pet;
				pet = (PetEntrar)inputs[cual].read();
				// miramos si es para la nave 0:
				if (pet.nave == 0) {
					entrar0.add(pet);
				} else {
					entrarN[pet.nave] = pet;
				}
			} else { // es peticion de salir...
				// leemos la peticion y el canal nos proporciona el
				// numero de nave:
				int que_peso = (Integer)inputs[cual].read();
				int que_nave = cual;
				// si el mensaje se ha aceptado, es porque se puede
				// salir...
				if (que_nave < N_NAVES - 1) {
					ocupado[que_nave + 1] = true;
					// actualizamos la condicion de recepcion
					sincCond[que_nave] = false;
				}
				// actualizamos el peso de la nave
				peso[que_nave] -= que_peso;
			}
			// TRATAMIENTO DE PETICIONES APLAZADAS
			// solo se aplazan las peticiones de entrar
			// nave 0:
			while(!entrar0.isEmpty()){//Mientras la cola no este vacia...
				PetEntrar peticion = entrar0.peek();//Recuperamos pero no eliminamos la cabeza de la cola.Si es vacia pone un null
				if (peticion != null) {//Es necesario que la cola tenga peticiones
					if (peso[0] + peticion.peso <= Robots.MAX_PESO_EN_NAVE) {//Comprobamos la condicion de entrada en la nave 0
						peso[0] += peticion.peso;				   //Hacemos el POST y
						entrar0.remove();						   //eliminamos de la cola esa peticion. 
						peticion.cresp.out().write(peticion.peso); //Respondemos a esa peticion(Nos da igual lo que lleve dentro asique ponemos un null por defecto)
					}
					else break;	//Si no se cumple la condicion salimos del bucle
				}
			}
			// resto de naves, a razón de una peticion por nave:
			for (int i=0; i<entrarN.length; i++) {		//Podriamos empezar en i=1 ya que i=0 ya se trata ese caso en la nave 0
				if (entrarN[i] != null) {			    //Es necesario tener peticiones
					if (peso[i] + entrarN[i].peso <= Robots.MAX_PESO_EN_NAVE) {//Comprobamos la condicion de entrada en el resto de naves
						peso[i] += entrarN[i].peso;		//Hacemos el POST
						ocupado[i] = false;
						sincCond[i-1] = true;
						entrarN[i].cresp.out().write(entrarN[i].peso); //Respondemos a esa peticion
						entrarN[i] = null;							   //Como no queremos que esa peticion siga procesandose la ponemos a null
					}
				}
			}
		} // FIN BUCLE SERVIDOR
	} // FIN SERVIDOR

}