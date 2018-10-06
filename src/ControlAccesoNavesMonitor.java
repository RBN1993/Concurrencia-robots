/*
-----------AUTOR:ROBINSON GANCHALA BENITEZ 110377, 54267793Y-----------------
 */
import net.datastructures.NodePositionList;
import net.datastructures.Position;
import net.datastructures.PositionList;
import es.upm.babel.cclib.Monitor;

public class ControlAccesoNavesMonitor implements ControlAccesoNaves{ //Implementamos a la interfaz ControlAccesoNaves

	private int peso[] = new int[Robots.N_NAVES];				//Array de pesos de las naves
	private int pesoPasillo[] = new int[Robots.N_NAVES-1];		//Array peso en pasillo(tenemos n pasillos - 1)
	private boolean ocupado[] = new boolean[Robots.N_NAVES-1];	//Array para comprobar la ocupacion de pasillos(tenemos n pasillos - 1)
	private PositionList<Integer> listaPesos0 = new NodePositionList<Integer>();//Lista para guardar pesos de Robots que no entran en nave cero
	Monitor mutex = new Monitor();								//Monitor mutex
	Monitor.Cond entrar[]= new Monitor.Cond[Robots.N_NAVES-1];	//Dejar pasillo(tenemos n pasillos - 1)
	Monitor.Cond salir[] = new Monitor.Cond[Robots.N_NAVES-1];	//Entrar pasillo(tenemos n pasillos - 1)
	Monitor.Cond entrar0[]= new Monitor.Cond[Robots.MAX_PESO_EN_NAVE];//Entrar en nave cero
	Monitor.Cond pesoAlto= mutex.newCond();						//Entrar en nave cero

	//----Valores iniciales-----//	

	public ControlAccesoNavesMonitor() {				//Constructor de ControlAccesoNavesMonitor 
		for (int i=0; i<Robots.N_NAVES; i++) {			//Bucle para inicializar el peso de las naves
			peso[i] = 0;								//Inicializamos los pesos a cero
		}
		for(int i=0;i<Robots.N_NAVES-1;i++){			//Bucle para inicializar:
			pesoPasillo[i]=0; 							//El peso de los pasillos a cero
			ocupado[i] = false;							//Las condiciones de ocupacion de los pasillo a false
			salir[i] = mutex.newCond();					//El monitor de condicion de entrar en pasillo(salir de la nave) 
			entrar[i] = mutex.newCond();				//El monitor de condicion de dejar el pasillo(entrar en nave) 
		}
		for (int i=0; i<Robots.MAX_PESO_EN_NAVE; i++){  //Bucle para inicializar:
			entrar0[i]= mutex.newCond();				//El monitor de condicion para entrar en la nave cero
		}
	}

	@Override
	public void solicitarEntrar(int n, int p) {
		mutex.enter();
		if (!(p + peso[n] <= Robots.MAX_PESO_EN_NAVE)) {//Si no se cumple la condicion de entrada el robot se tiene que esperar
			if(n==0){									//Para el caso de la nave entrada nave cero

				if (p>Robots.MAX_PESO_EN_NAVE) {		//Si el peso que intenta entrar es mayor que el peso maximo de la nave 	
					pesoAlto.await();					//Se bloquea.
				}
				else {
					listaPesos0.addLast(p);				//Si no se cumple la condicion de entrada lo ponemos en la cola	
					entrar0[p-1].await();				//Ponemos a esperar el robot 
				}
			}
			else if (n>0){
				pesoPasillo[n-1]=p;						//Guardamos el peso que hay en el pasillo(el del robot)						
				entrar[n-1].await();					//Ponemos en espera al robot
			}
		}
		//Cuando se cumple la condicion Implementamos el POST

		peso[n] += p;              //Sumamos el peso del robot al peso de la nave 

		if (n>0){ 		           //Para el caso de nave distintas de la primera(nave 0) 		
			ocupado[n-1] = false;  //ponemos a false el pasillo
			pesoPasillo[n-1]=0;    //Ponemos el peso del pasillo a cero tras ser liberado el robot que estaba en el pasillo
			salir[n-1].signal();   //Notificamos que se puede entrar en pasillo(salir de la nave anterior)
		}						   //El primer pasillo no se tiene en cuenta,no existe
		else if (n==0) {												  //Para el caso de la nave 0
			int aux=0;													  //Variable auxiliar para guardar el peso	
			for(Position<Integer> puntero:listaPesos0.positions()){		  //Recorremos la lista para buscar el robot que cumpla	la condicion
				if(puntero.element()+peso[n] <= Robots.MAX_PESO_EN_NAVE){ //Si se cumple la condicion
					aux=puntero.element();								  //Guardamos el peso
					entrar0[aux-1].signal();							  //Liberamos al robot con ese peso
					listaPesos0.remove(puntero);						  //Quitamos el peso
					break;												  //Salimos del bucle	
				}
			}
		}	
		mutex.leave();			

	}

	@Override
	public void solicitarSalir(int n, int p) {
		mutex.enter();
		if(!(n == Robots.N_NAVES-1 || ocupado[n] == false)) {  //Si no se cumple la condicion el robot se espera
			salir[n].await();								   //Ponemos a esperar el robot con peso que no entra									  
		}
		//Cuando se cumple la condicion Implementamos el POST

		peso[n] -= p;				  							//Cuando se cumple la condicion
		if(n<Robots.N_NAVES-1)		  							//Implementamos el POST
			ocupado[n] = true;     								 //El ultimo pasillo se toma como si no existiese
		//Si el peso que intenta entrar mas el peso de la nave actual no supera el peso maximo notificamos
		if(n>0 && (pesoPasillo[n-1]+peso[n] <= Robots.MAX_PESO_EN_NAVE)){	
			entrar[n-1].signal();			  					//Notificamos a las naves intentando entrar en la nave N que un robot ha salido
		}
		else if (n==0) {												  //Para el caso de la nave 0
			int aux=0;													  //Variable auxiliar para guardar el peso	
			for(Position<Integer> puntero:listaPesos0.positions()){		  //Recorremos la lista para buscar el robot que cumpla	la condicion
				if(puntero.element()+peso[n] <= Robots.MAX_PESO_EN_NAVE){ //Si se cumple la condicion
					aux=puntero.element();								  //Guardamos el peso
					entrar0[aux-1].signal();							  //Liberamos al robot con ese peso
					listaPesos0.remove(puntero);						  //Quitamos el peso
					break;												  //Salimos del bucle	
				}
			}
		}	
		mutex.leave();
	}
}
