
import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur extends JFrame {
    private char[] panneau;
    private JTextArea outputArea;
    private Joueur[] Joueurs;
    private ServerSocket server;
    private Boolean reset = false;
    private boolean rematchButton = false;
    private boolean rematchGame = true;
    private int currentJoueur;
    private int lastmove;
    private int count;
    private final int JOUEUR_X = 0, JOUEUR_O = 1;
    private final char X_Marque = 'X', O_Marque = 'O';
    private String Joueur1 = "JOUEUR_X";
    private String Joueur2 = "JOUEUR_O";
    private String gagnant = "";
    private final String resultat = "resultat:";
  
public  boolean etat ;

    public Serveur()
    {
        super( "GOMUKU Server" );

        panneau = new char[ 361 ];
        Joueurs = new Joueur[ 2 ];
        currentJoueur = JOUEUR_X;

      
        try {
            server = new ServerSocket( 1111, 2 );
        }

      
        catch( IOException ioException ) {
            ioException.printStackTrace();
            System.exit( 1 );
        }

       
        outputArea = new JTextArea();
        
        getContentPane().add( new JScrollPane( outputArea ), BorderLayout.CENTER );
        outputArea.setText( "Server en attente de connections \n" );

        setSize( 300, 300 );
        setVisible( true );

    } 
   
    public void executer()
    {
       
        for ( int i = 0; i < Joueurs.length; i++ ) {

            
            try {
                Joueurs[ i ] = new Joueur( server.accept(), i );
                Joueurs[ i ].start();
            }

           
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }
        }

       
        synchronized ( Joueurs[ JOUEUR_X ] ) {
            Joueurs[ JOUEUR_X ].setSuspended( false );
            Joueurs[ JOUEUR_X ].notify();
        }

    }  
    private void afficherMessage( final String message )
    {
        
        SwingUtilities.invokeLater(new Runnable() {  
                                       public void run() 
                                       {
                                    	   
                                           outputArea.append( message );
                                           outputArea.setCaretPosition(
                                                   outputArea.getText().length() );
                                       }
                                   }  

        ); 
    }

    
    
    
    
    
    
   
    
    public synchronized boolean mouvemantValable( int location, int Joueur )
    {
       
        while ( Joueur != currentJoueur ) {

          
            try {
                wait();
            }

            catch( InterruptedException interruptedException ) {
                interruptedException.printStackTrace();
            }
        }

       
        if ( !Occupee( location ) && isGameOver(location)==false ) {

            panneau[ location ] = currentJoueur == JOUEUR_O ? X_Marque : O_Marque;
            lastmove=location;
         
            currentJoueur = ( currentJoueur + 1 ) % 2;

          
            Joueurs[ currentJoueur ].otherJoueurMoved( location );

            notify(); 
            return true;
        }

       
        else
            return false;

    } 
    public boolean Occupee( int location )
    {
        if ( panneau[ location ] == X_Marque || panneau [ location ] == O_Marque )
            return true;
        else
            return false;
    }
 
    private int Verifier( int row, int col, int dirX, int dirY,int  Joueur) {
        int compteur = -1;
        int r, c;   
        r = row ; 
        c = col ;
        while ( r >= 0 && r < 19 && c >= 0 && c < 19 && panneau[r*19+c] == Joueur ) {
            compteur++;
            r += dirX;  
            c += dirY;
        }
       r = row ;  
       c = col ;
       while ( r >= 0 && r < 19 && c >= 0 && c < 19 && panneau[r*19+c] == Joueur ) {    
            compteur++;
            r -= dirX;  
            c -= dirY;
        }
        return compteur;

    }
    public boolean isGameOver(int location)
    {
    	int row = location /19;
    	int col;
    	if (row==0) {
			col= location;
		}else {
			col=location-row*19;
		}
    	 if (Verifier(  row, col, 1, 0,X_Marque ) >= 5 || Verifier( row, col, 0, 1,X_Marque ) >= 5||Verifier( row, col, 1, -1,X_Marque ) >= 5||Verifier(row, col, 1, 1,X_Marque ) >= 5) {
    		 etat = true;
    		 gagnant = Joueur2;
             return true;
    	 }
   
    	 if (Verifier(  row, col, 1, 0,O_Marque ) >= 5 || Verifier( row, col, 0, 1,O_Marque ) >= 5||Verifier( row, col, 1, -1,O_Marque ) >= 5||Verifier(row, col, 1, 1,O_Marque ) >= 5) {
    		 etat = true;
    		 gagnant = Joueur1;
             return true;
    	 }
    	
        if(count == 361)
        {
        	etat=true;
            gagnant = "aucun";
            
            return true;
        }
        etat=true;
        return false;
    }

    public static void main( String args[] )
    {
        Serveur application = new Serveur();
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        application.executer();
    }

// class Joueur
    private class Joueur extends Thread {
        private Socket connection;
        private DataInputStream input;
        private DataOutputStream output;
        private int JoueurNumber;
        private char Marque;
        protected boolean suspended = true;

        public Joueur( Socket socket, int number )
        {
            JoueurNumber = number;

          
            Marque = ( JoueurNumber == JOUEUR_X ? X_Marque : O_Marque );

            connection = socket;

            
            try {
                input = new DataInputStream( connection.getInputStream() );
                output = new DataOutputStream( connection.getOutputStream() );
            }

            
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }

        } 
        
        public void otherJoueurMoved( int location )
        {
            
            try
            {
                if(isGameOver(location))
                {
                    if(gagnant == "aucun")
                        output.writeUTF(resultat+" "+gagnant);
                    else
                        output.writeUTF(resultat+" "+gagnant+" a gagné");
                }
                else
                {
                    output.writeUTF("adversaire a jooué");
                    output.writeInt(location);
                    lastmove=location;
                }
            }

            
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
        }

       
        public void run() {
            
        	
            try {
                afficherMessage("JOUEUR  " + (JoueurNumber ==
                        JOUEUR_X ? X_Marque : O_Marque) + " a conecté\n");

                output.writeChar(Marque); 
                
                output.writeUTF("JOUEUR " + (JoueurNumber == JOUEUR_X ?
                        "X conecté" : "O conecté, attendez votre tour"));

                
                if (Marque == X_Marque) {
                    output.writeUTF("en attente d'un autre joueur");

                   
                    try {
                        synchronized (this) {
                            while (suspended)
                                wait();
                        }
                    }

                  
                    catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }

                   
                    output.writeUTF("autre jouer est connecter. JOUER.");
                }
                
                while (rematchGame) {
                   
                    while (isGameOver(lastmove)==false)
                    {
                        
                    	int location = input.readInt();

                        
                        if (mouvemantValable(location, JoueurNumber)) {
                            afficherMessage("\nlocation: " + location);
                            output.writeUTF("Mouvement valide.");
                          
                            count++;
                            
                        }
                        else
                            output.writeUTF("Mouvement non valide essayer a nouveau");
                    }
                    if (gagnant == "aucun")
                    {
                        afficherMessage("\n" + resultat + gagnant);
                        output.writeUTF(resultat + " " + gagnant);
                        afficherBtn(input);
                    }
                    else
                    {
                        afficherMessage("\n" + resultat + gagnant + " a gagné");
                        output.writeUTF(resultat + " " + gagnant + " a gagné");
                        afficherBtn(input);
                    }
                }
                connection.close(); 
            }

           
            catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }
        

       
        public void setSuspended( boolean status )
        {
            suspended = status;
        }

    } 

    public void afficherBtn(DataInputStream input){
        try {
            while(reset == false) {
                if (rematchButton == false) {

                    Joueurs[currentJoueur].output.writeUTF("partiefinie");
                    currentJoueur = (currentJoueur + 1) % 2;
                    Joueurs[currentJoueur].output.writeUTF("partiefinie");
                    rematchButton = true;
                }
               
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
} 
