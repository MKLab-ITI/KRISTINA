package TopicDetection.funs;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class FileFunctions 
{
	/**
	 * Create a file and insert its content by giving its path and filename.
	 * 
	 * @param filename		string consisting of the directory and filename of the file to be created
	 * @param fileContent	string containing the content of the file to be created
	 * 
	 * @throws IOException
	 */ 
	public void createFile(String filename,String fileContent) throws IOException
	{
		File fi = new File(filename);
		fi.createNewFile();

		BufferedWriter buffWriter = new BufferedWriter(new FileWriter(fi));
		buffWriter.write(fileContent);
		buffWriter.close();
	}


	/**
	 * Reading the content of a file by giving its path and filename.
	 *  
	 * @param filename		string consisting of the directory and filename of the file to be created
	 * 
	 * @return an array of string containing the file
	 * 
	 * @throws IOException
	 */ 
	public String[] readFile(String filename) throws IOException
	{
		int line=0;
		String thisLine = "";
		String[] wholeFile = null;

		int numOfElements = getFileLines(filename);
		wholeFile = new String[numOfElements];

		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		while ( (thisLine = br.readLine())!=null) 
		{
			wholeFile[line] = thisLine;
			line = line+1;
		}

		br.close();

		return wholeFile;
	}



	/**
	 * Getting the number of lines of a file by giving its path and filename.
	 *  
	 * @param filename		string consisting of the directory and filename of the file to be created
	 * 
	 * @return integer referring to the number of lines of the file.
	 * 
	 * @throws IOException
	 */ 
	public int getFileLines(String filename) throws IOException
	{
		int line=0;
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);

		while ( br.readLine()!=null) 
		{
			line = line+1;
		}
		br.close();

		return line;
	}


	/**
	 *  Open a file to write new content by giving its path and filename.
	 *  
	 * @param filename		string consisting of the directory and filename of the file to be created
	 * 
	 * @return BufferedWriter
	 * 
	 * @see #writeFile(BufferedWriter bw, String content)
	 * @see #closeFile(BufferedWriter bw)
	 * 
	 * @throws IOException
	 */ 
	public BufferedWriter openFile(String filename) throws IOException  
	{
		BufferedWriter bw = null;
		File fi = new File(filename);
		fi.createNewFile();
		bw = new BufferedWriter(new FileWriter(fi));	
		return bw;
	}


	/**
	 *  Open a file in order to append content to it, by giving its path and filename.
	 *  
	 * @param filename		string consisting of the directory and filename of the file to be created
	 * 
	 * @return BufferedWriter
	 * 
	 * @see #writeFile(BufferedWriter bw, String content)
	 * @see #closeFile(BufferedWriter bw)
	 * 
	 * @throws IOException
	 */ 
	public BufferedWriter openFileToAppend(String filename) throws IOException
	{
		BufferedWriter bw = null;
		File fi = new File(filename);
		fi.createNewFile();
		bw = new BufferedWriter(new FileWriter(fi,true));	
		return bw;
	}


	/**
	 * Writing to a file by giving its BufferedWriter (used together with openFile or openFileToAppend)
	 *  
	 * @param bw		BufferedWriter created by openFile or openFileToAppend function in order to write to the openedFile
	 * @param content	string string containing the content of the file to be created
	 * 
	 * @see #openFile(String filename) 
	 * @see #openFileToAppend(String filename)
	 * @see #closeFile(BufferedWriter bw)
	 * 
	 * @throws IOException
	 */ 
	public void writeFile(BufferedWriter bw, String content) throws IOException
	{
		bw.write(content);
		bw.flush();
	}


	/**
	 *  Close an opened file.
	 *  
	 * @param bw		BufferedWriter created by openFile or openFileToAppend function in order to write to the openedFile
	 * 
	 * @see #openFile(String filename) 
	 * @see #openFileToAppend(String filename)
	 * @see #writeFile(BufferedWriter bw, String content)
	 * 
	 * @throws IOException
	 */ 
	public void closeFile(BufferedWriter bw) throws IOException
	{
		bw.close();
	}




	/**
	 *  Delete all files and subfolders existing in a directory.
	 *  
	 * @param dir		Directory containing all files etc.
	 *  
	 */ 
	public boolean deleteDir(File dir)
	{
		if (dir.isDirectory()) 
		{
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) 
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) 
				{
					return false;
				}
			}
		}

		return dir.delete();
	}


	/**
	 *  Delete all files existing in a directory.
	 * @param directory		Directory containing all files etc.
	 */ 
	public void emptyFolders(File directory)
	{
		String[] fileList = directory.list();
		File f = null;

		for(int i=0;i<fileList.length;i++)
		{
			f = new File(directory.getPath()+"\\"+fileList[i]);
			f.delete();
		}
	}


	/**
	 *  Delete directory
	 * @param directory		Directory containing all files etc.
	 */ 
	public void deleteFolder(File directory)
	{
		File f = new File(directory.getPath());
		f.delete();
	}


}