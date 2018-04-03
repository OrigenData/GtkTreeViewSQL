package prog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import org.gnome.gdk.Event;
import org.gnome.gtk.Builder;
import org.gnome.gtk.Gtk;
import org.gnome.gtk.ListStore;
import org.gnome.gtk.MessageDialog;
import org.gnome.gtk.MessageType;
import org.gnome.gtk.ResponseType;
import org.gnome.gtk.TreeIter;
import org.gnome.gtk.TreeView;
import org.gnome.gtk.TreeViewColumn;
import org.gnome.gtk.Widget;
import org.gnome.gtk.Window;
import org.gnome.gtk.Button;
import org.gnome.gtk.CellRendererText;
import org.gnome.gtk.DataColumnString;
import org.gnome.gtk.Button.Clicked;
import org.gnome.gtk.ButtonsType;
import org.gnome.gtk.Entry;
import org.gnome.gtk.Window.DeleteEvent;

// **********  SQL ******** //
import java.sql.*;



public class TabularForm implements Constans{
	
    Builder builder;
    Window window;
    Button buttonAdd, buttonUpdate;
    Entry entryName, entryAge;
    TreeView view;
    TreeViewColumn vertical;
    TreeIter row;
    ListStore listStore;
    DataColumnString idColumn, nameColumn, ageColumn;
    CellRendererText idText, nameText, ageText;
    String captureName, captureAge ;
	
	Connection DB =null;
	Statement st = null;
	
	public TabularForm() {
		
		copyResource();
		
        try {
        	
        	builder = new Builder();
			builder.addFromFile(FOLDER_DIR+"/GUI.glade");
			
		} catch (FileNotFoundException | ParseException e) {
			
			System.err.println(e.getMessage());

		}

 
		getObjectGlade();
        window.showAll();
		
	}
	
	public void getObjectGlade() {
		
        //Window
        window = (Window) builder.getObject("windowID");
        window.connect(on_windowID_destroy());
 
        //Cajas de texto
        entryName = (Entry) builder.getObject("entryNombreID");
        entryAge = (Entry) builder.getObject("entryEdadID");
 
        //Botones
        buttonAdd = (Button) builder.getObject("buttonAnadirID");
        buttonAdd.connect(on_buttonAnadirID_clicked());
 
        buttonUpdate = (Button) builder.getObject("buttonActualizarID");
        buttonUpdate.connect(on_buttonActualizarID_clicked());
 
        //TreeView
        view = (TreeView) builder.getObject("treeviewID");
 
        /* Construccion del modelo */
        listStore = new ListStore(new DataColumnString[]{
 
                idColumn = new DataColumnString(),
                nameColumn = new DataColumnString(),
                ageColumn = new DataColumnString(),
        });
 
        /*Establezca TreeModel que se usa para obtener datos de origen para este TreeView*/
        view.setModel(listStore);
 
        /*Crear instancias de TreeViewColumn*/
        vertical = view.appendColumn();
        vertical.setTitle("ID");
        idText  = new CellRendererText(vertical);
        idText.setText(idColumn);
        
        vertical = view.appendColumn();
        vertical.setTitle("Nombre");
        vertical.setExpand(true);
        nameText = new CellRendererText(vertical);
        nameText.setText(nameColumn);
 
        vertical = view.appendColumn();
        vertical.setTitle("Edad");
        ageText = new CellRendererText(vertical);
        ageText.setText(ageColumn);
		 
	}
	
	private DeleteEvent on_windowID_destroy() {
		return new Window.DeleteEvent() {
			
			@Override
			public boolean onDeleteEvent(Widget arg0, Event arg1) {
				Gtk.mainQuit();
				return false;
			}
		};
		
	}
	
	
	private Clicked on_buttonAnadirID_clicked() {
		return new Button.Clicked() {
			
			@Override
			public void onClicked(Button arg0) {
				
				//Acciones del boton Agregar

                try {
			        //Conexion con la base de datos
			        DB = DriverManager.getConnection(URL, DBUSER, DBPASSWD);
			 
			        // Se hara una consulta  de la tabla CDS y Cantante, y se mandara a imprimir.
			        st = DB.createStatement();
			        
			        st.executeUpdate( "INSERT INTO \"Persona\" (\"perNombre\", \"perEdad\") "
			        		+ "VALUES ('"+entryName.getText().toUpperCase()+"' , '"+entryAge.getText()+"');" );
			        
			        st.close();
			        DB.close();
			    } catch (SQLException e) {
			        System.err.println("Error: " +e.getMessage() );
			 
			    }
                
                
                entryName.setText("");
                entryAge.setText("");
                

			}
		};
		
	}
	
	private Clicked on_buttonActualizarID_clicked() {
		return new Button.Clicked() {
			
			@Override
			public void onClicked(Button arg0) {
				
				//Acciones del boton Actualizar
				
				MessageDialog dialog = new MessageDialog(window, true, MessageType.WARNING, ButtonsType.CLOSE,"Se han agregado nuevos valores");
				dialog.setSecondaryText("La tabla tiene nuevos valores, presione el boton cerrar para continuar");
				 
				 // Oculta el dialogo
				 ResponseType choice = dialog.run();
				 if (choice == ResponseType.CLOSE) {
					 dialog.hide();
				 }
				 
				 listStore.clear(); //Limpiar TreeView
				 
				    try {
				        //Conexion con la base de datos
				        DB = DriverManager.getConnection(URL, DBUSER, DBPASSWD);
				 
				        // Se hara una consulta  de la tabla CDS y Cantante, y se mandara a imprimir.
				        st = DB.createStatement();
				        
				        
				        ResultSet rs = st.executeQuery( "SELECT * FROM \"Persona\"; " );
				        
				        
				        while    ( rs.next() ) {
				        	
				        	row = listStore.appendRow();
				        	listStore.setValue(row, idColumn, rs.getString("perID"));
			                listStore.setValue(row, nameColumn, rs.getString("perNombre"));
			                listStore.setValue(row, ageColumn, rs.getString("perEdad"));
				        }
				 
				        rs.close();
				        st.close();
				        DB.close();
				        
				    } catch (SQLException e) {
				        System.err.println( e.getMessage() );
				 
				    }
				
			}
		};
		
	}
	
	
    public void copyResource() {
    	 
        File folder = new File(FOLDER_DIR);
        folder.mkdirs();
 
        try {
 
            InputStream input=getClass().getResourceAsStream("/gui/GUI.glade");
            OutputStream output= new FileOutputStream(FOLDER_DIR+"/GUI.glade");
            byte [] buffer = new byte[1024];
            int bytesRead;
 
              while ((bytesRead = input.read(buffer,0,1024)) != -1) {
 
                    output.write(buffer, 0, bytesRead);
                }
 
              output.close();
              input.close();
 
         } catch(IOException e) {
            System.err.println(""+e.getMessage());
         }
      }
	

	public static void main(String[] args) {
		
		Gtk.init(args);
		new TabularForm();
		Gtk.main();

	}

}
