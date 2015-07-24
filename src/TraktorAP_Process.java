import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author recurza
 * Date: 24-07-2015
 * License: GPL
 */

public class TraktorAP_Process
{
	private String[][] playlists;
	private Scanner sc = null;
	private PrintWriter pw = null;

	public TraktorAP_Process(String[][] playlists)
	{
		this.playlists = playlists;
		String path = TraktorAP.conf.get("path") + "collection.nml";
		try
		{
			sc = new Scanner(new FileInputStream(path), "UTF-8");
			pw = new PrintWriter(new FileWriter(new File(path + ".temp"), false));
		}
		catch (Exception e) { System.err.println("Process: File Error!\t" + e.getMessage()); }
		parse();
		sc.close();
		pw.close();

		try
		{
			Files.move(FileSystems.getDefault().getPath(path), FileSystems.getDefault().getPath(path + ".backup"), StandardCopyOption.REPLACE_EXISTING);
			Files.move(FileSystems.getDefault().getPath(path + ".temp"), FileSystems.getDefault().getPath(path), StandardCopyOption.REPLACE_EXISTING);

			if(System.getProperty("os.name").contains("Mac"))
				new ProcessBuilder("open", TraktorAP.conf.get("trak")).start();
			else
				new ProcessBuilder(TraktorAP.conf.get("trak")).start();
		}
		catch (IOException e) { System.err.println("IO Exception!\t" + e.getMessage()); }
		System.exit(0);
	}

	public void parse()
	{
		while (sc.hasNextLine())
		{
			String a = sc.nextLine();
			if (a.contains("TYPE=\"PLAYLIST\""))
				put(a, getVal(a, "TYPE=\"PLAYLIST\" NAME"));
			else if (a.contains("<ENTRY") && !a.contains("PRIMARYKEY"))
			{
				String b = sc.nextLine();
				while (!b.contains("</ENTRY>"))
				{
					a += b;
					b = sc.nextLine();
				}//TODO reload rules on startup
				//TODO key modified date
				match:
				for (int r = 0; r < playlists.length; r++)
				{
					for (int c = 2; c < playlists[r].length; c += 3)
						if (!hasPattern(a.toUpperCase(), toI(playlists[r][c]), toI(playlists[r][c + 1]), playlists[r][c + 2]))
							continue match;
					playlists[r][1] += "<ENTRY><PRIMARYKEY TYPE=\"TRACK\" KEY=\"" + getVal(a, "VOLUME") + getVal(a, "DIR") + getVal(a, "FILE") +
							"\"></PRIMARYKEY></ENTRY>";
				}
				pw.println(a+"</ENTRY>");
			}
			else if (!a.contains("PRIMARYKEY") && !a.contains("</ENTRY>")) pw.println(a);
		}
	}

	public boolean hasPattern(String a, int f, int op, String var)
	{
		var = var.toUpperCase();
		String field = TraktorAP.fields[f], pNorm, pRegex="";

		if(f == 0)
		{
			pNorm = var + "\"></ALBUM>";
			Matcher m = Pattern.compile("ALBUM.*\"(.*?)\"></ALBUM>").matcher(a);
			if(m.find()) pRegex = m.group(1);
		}
		else
		{
			pNorm = field + "=\"" + var;
			pRegex = getVal(a, field);
		}

		switch (op)
		{
			case 0: return a.contains(pNorm);
			case 1: return !a.contains(pNorm);
			case 2: return pRegex.contains(var);
			case 3: return !pRegex.contains(var);
			case 4: return toI(var) >= Math.round(Double.parseDouble(getVal(a, field)));
			case 5: return toI(var) <= Math.round(Double.parseDouble(getVal(a, field)));
		}
		return false;
	}
	public void put(String a, String title)
	{
		int index = getIndex(title);
		if(index != -1)
		{
			boolean p = false;
			if (a.contains("</PLAYLIST>"))
			{
				a = a.replace("</PLAYLIST>", "");
				p = true;
			}
			if (a.contains("<ENTRY>"))
				a = a.split("<ENTRY>")[0];
			pw.println(a);
			if (playlists[index].length > 2)
				pw.println(playlists[index][1]);
			if (p)
				pw.println("</PLAYLIST>");
		}
		else
		{
			pw.println(a);
		}
	}

	public static String getVal(String h, String n, String... a)
	{
		Matcher m = Pattern.compile(n + "=\"(.*?)\"").matcher(h);
		if(m.find())
			return m.group(1);
		return "0";
	}

	public int getIndex(String v)
	{
		for (int i = 0; i< playlists.length; i++)
			if(playlists[i][0].equals(v))
				return i;
		return -1;
	}

	/*public boolean isNew(String a, String d)
	{
		String[] imp = a.split("/");
		String[] run = d.split("/");
		try
		{
			for (int i = 0; i < 2; i++)
				if (!(toI(imp[i]) >= toI(run[i])))
					return false;
		} catch (Exception e){ return true;}

		return true;
	}
*/
	public int toI(String a){ try{return Integer.parseInt(a);} catch (Exception e){log("NaN, a");return 0;}}
	public void log(Object... k) { for (Object b : k) System.out.print(b.toString() + "\t"); System.out.println();}
}
//TODO threads (maybe)