
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {

    private JTextField idField;
    private JTextArea affichage;
    private JButton btn = new JButton();
    private JPanel panneauPanel, panel2;
    private Case panneau[][], currentCase;
    private Socket connection;
    private DataInputStream input;
    private DataOutputStream output;
    private char myMarque;
    private boolean myTurn;
    private final char X_Marque = 'X', O_Marque = 'O';
   
private JPanel panel3;
private JPanel panel4;
    public Client()
    {
        super( "GOMOKU CLIENT" );

     
        try {
            connection = new Socket( InetAddress.getLocalHost(), 1111);

            input = new DataInputStream( connection.getInputStream() );
            output = new DataOutputStream( connection.getOutputStream() );
           
        }

       
        catch( IOException ioException ) {
            ioException.printStackTrace();
            System.exit( 1 );
        }

      
        Thread outputThread = new Thread();
        outputThread.start();

        Container container = getContentPane();

       
        affichage = new JTextArea( 5, 50 );
        affichage.setEditable( false );
        container.add( new JScrollPane( affichage ), BorderLayout.SOUTH );

       
        panneauPanel = new JPanel();
       
        panneauPanel.setLayout( new GridLayout( 19, 19, 0, 0 ) );

       
        panneau = new Case[ 19 ][ 19 ];

        
        for ( int row = 0; row < panneau.length; row++ ) {

            for ( int column = 0; column < panneau[ row ].length; column++ ) {

                panneau[ row ][ column ] = new Case( ' ', row * 19 + column );
                panneau[ row ][ column ].setBackground(Color.white);
                panneauPanel.add(panneau[ row ][ column ] );
            }
        }

      
        idField = new JTextField();
        idField.setEditable( false );
        container.add( idField, BorderLayout.NORTH );

        
        panel2 = new JPanel();
       

       
        panel2.add( panneauPanel, BorderLayout.CENTER );
        container.add( panel2, BorderLayout.CENTER );
        
     
       
        setSize( 700, 770 );
        setVisible( true );

   
    } 
    
    public void run()
    {
        
        try {
            myMarque = input.readChar();
           
          
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run()
                        {
                            idField.setText( " LE JOUEUR \"" + myMarque + "\"" );
                        }
                    }
            );

            myTurn = ( myMarque == X_Marque ? true : false );

           
            while ( true ) {
                traiterMessage( input.readUTF() );
            }

        } 
        
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }

    }  
    
    private void traiterMessage( String message )
    {
       
    	
        if ( message.equals( "Mouvement valide." ) ) {
            AfficherMessage( "Mouvement valide, attendez.\n" );
            setMarque( currentCase, myMarque );
        }

        
        
        else if ( message.equals( "Mouvement non valide essayer a nouveau" ) ) {
            AfficherMessage( message + "\n" );
            myTurn = true;
        }

       
        else if ( message.equals( "adversaire a joou�" ) ) {

            
        	
            try {
                int location = input.readInt();
                int row = location / 19;
                int column = location % 19;

                setMarque(  panneau[ row ][ column ],
                        ( myMarque == X_Marque ? O_Marque : X_Marque ) );
                AfficherMessage( "adversaire a joou�. ton tour.\n" );
                myTurn = true;

            } 
            
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }

        } 
        
        else if (message.equals("JOUEUR_X a gagn�") ||
                message.equals("JOUEUR_O a gagn�")||
                message.equals("aucun"))
        {
            AfficherMessage(message + "\n");
            myTurn = false;
        }
        else if(message.equals("partiefinie")){
            panel2.add(btn);
            btn.setBounds(30,20,60,30);
            btn.setText("partie terminer");
            btn.setVisible(true);
            btn.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(e.getSource().equals(btn))
            {
                try
                {
                    
                	System.exit(0);
                    
                }
                catch (Exception r)
                {
                    r.printStackTrace();
                }
            }
                }
            });
            
        }
      
      
        else
            AfficherMessage( message + "\n" );

    } 

    private void AfficherMessage( final String msg )
    {
        
        SwingUtilities.invokeLater(
                new Runnable() {  
                    public void run() 
                    {
                        affichage.append( msg );
                        affichage.setCaretPosition(
                                affichage.getText().length() );
                    }

                }  
        ); 
    }

   
    
    private void setMarque( final Case CaseToMarque, final char Marque )
    {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run()
                    {
                        CaseToMarque.setmarque( Marque );
                    }
                }
        );
    }

    // envoyer message au server pour indiquer la case cliqu�
    public void sendClickedCase( int location )
    {
        if ( myTurn ) {

            // envoyer location au server
            try {
                output.writeInt( location );
                myTurn = false;
            }

            
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
        }
    }

    
    public void setCurrentCase( Case Case )
    {
        currentCase = Case;
    }

    // Class case
    private class Case extends JPanel {
        private char marque;
        private int location;

        public Case( char Casemarque, int CaseLocation )
        {
            marque = Casemarque;
            location = CaseLocation;

            addMouseListener(
                    new MouseAdapter() {
                        public void mouseReleased( MouseEvent e )
                        {
                            setCurrentCase( Case.this );
                            sendClickedCase( getCaseLocation() );
                        }
                    }
            );

        } 
        
        public Dimension getPreferredSize()
        {
            return new Dimension( 30, 30 );
        }

        
        public Dimension getMinimumSize()
        {
            return getPreferredSize();
        }

        
        public void setmarque( char newmarque )
        {
            marque = newmarque;
            repaint();
        }

        
        public int getCaseLocation()
        {
            return location;
        }

       
        public void paintComponent( Graphics g )
        {
            super.paintComponent( g );

            g.drawRect( 0, 0, 29, 29 );
            g.drawString( String.valueOf( marque ), 11, 20 );
            
        }

    } 

  
} 
