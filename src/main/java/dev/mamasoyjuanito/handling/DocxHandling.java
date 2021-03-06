package dev.mamasoyjuanito.handling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import dev.mamasoyjuanito.utils.Constantes;

public class DocxHandling {
	
	private Map<String, String> textsToReplace;
	private String originalFilePath;
	
	private String temporalFileName;
	
	private XWPFDocument doc;
	
	public DocxHandling() {}
	
	public DocxHandling(Map<String, String> textsToReplace, String originalFilePath, String destinationPath) {
		this.textsToReplace = textsToReplace;
		this.originalFilePath = originalFilePath;
	}
	
public InputStream replaceTexts() throws Exception {
	
	//Validamos que hayamos recibido el path del documento original 
	if(originalFilePath == null) throw new NullPointerException("Debe proporcionar el path del archivo original");
	
	//Validamos que hayamos recibido los textos a reemplazar 
	if(textsToReplace == null || textsToReplace.isEmpty()) throw new Exception("Debe proporcionar los textos a reemplazar");
	
	//Generamos un nombre para nuestro archivo temporal
	temporalFileName = UUID.randomUUID().toString();

	//Abrimos nuestro documento
	doc = new XWPFDocument(new FileInputStream(originalFilePath));

	//comenzamos con la iteracion de los textos a reemplazar
	Iterator it = textsToReplace.keySet().iterator();
	while(it.hasNext()) {
		String item = (String) it.next();
		//Enviamos a reemplazar el texto y guardamos sustituimos el documento en la misma variable
		doc = replaceText(doc, item, textsToReplace.get(item));
	}
	
	//Una vez se haya terminado de reemplazar, guardamos el documento en un archivo temporal
	//Para ello general el archivo temporal que pasaremos a nuestro metodo que se encarga de realizar la escritura
	String tmpDestinationPath = Files.createTempFile(temporalFileName, "." + Constantes.EXTENSION_DOCX).toAbsolutePath().toString();
	
	//Guardamos el documento en el archivo
	saveWord(tmpDestinationPath, 
			doc);
	
	//Retornamos un ImputStream por si el usuario va a trabajar con el
	return new FileInputStream(tmpDestinationPath);
}
	
	private InputStream replaceTextsAndConvertToPDF() throws Exception {
		try {
			InputStream in = replaceTexts();
			DocxToPdfConverter cwoWord = new DocxToPdfConverter();
	    	return cwoWord.convert(in);
		} catch (Exception e) {
			throw e;
		}
	}
	
    public static void main(String[] args){
    	System.out.println(UUID.randomUUID().toString());
    	if(true)
    		return;
    	
        String filePath = "/Users/jmenesesi/Desarrollo/Blackecco/Cingrow/Contratos/contrato_mandato.docx";
        String filePathDestino = "/Users/jmenesesi/Desarrollo/Blackecco/Cingrow/Contratos/contrato_mandato_destino_3.pdf";
             
        try {            
        	DocxHandling handling = new DocxHandling(); //(Collections.singletonMap("{nombre}", "Rolando Mota Del Campo"), filePath, 
        			//filePathDestino);
        	handling.setOriginalFilePath(filePath);
        	handling.setTextsToReplace(Collections.singletonMap("{nombre}", "Rolando Mota Del Campo"));
        	InputStream in = handling.replaceTextsAndConvertToPDF();
            File destino = new File(filePathDestino);
            Files.copy(in, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static XWPFDocument replaceText(XWPFDocument doc, String findText, String replaceText){
		//Realizamos el recorrido de todos los parrafos
        for (int i = 0; i < doc.getParagraphs().size(); ++i ) { 
			//Asignamos el parrafo a una variable para trabajar con ella 
            XWPFParagraph s = doc.getParagraphs().get(i); 
            
			//De ese parrafo recorremos todos los Runs
            for (int x = 0; x < s.getRuns().size(); x++) { 
				//Asignamos el run en turno a una varibale
                XWPFRun run = s.getRuns().get(x); 
				//Obtenemos el texto 
				String text = run.text();
				//Validamos si el texto contiene el key a sustituir 
				if(text.contains(findText)) {
					//Si lo contiene lo reemplazamos y guardamos en una variable
					String replaced = run.getText(run.getTextPosition()).replace(findText, replaceText);
					//Pasamos el texto nuevo al run
					run.setText(replaced, 0);
                }
            }
        } 
		//Retornamos el documento con los textos ya reemplazados
        return doc;
    }

	private static void saveWord(String filePath, XWPFDocument doc) throws FileNotFoundException, IOException{
		FileOutputStream out = null;
		try{
			out = new FileOutputStream(filePath);
			doc.write(out);
		}
		finally{
			out.close();
		}
	}

	public void setTextsToReplace(Map<String, String> textsToReplace) {
		this.textsToReplace = textsToReplace;
	}

	public void setOriginalFilePath(String originalFilePath) {
		this.originalFilePath = originalFilePath;
	}
    
    
}
