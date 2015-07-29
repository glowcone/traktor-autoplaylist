import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author recurza
 * Date: 24-07-2015
 * License: GPL
 */

public class TraktorAP extends JFrame
{
	static Map<String, String> conf = new HashMap<>();
	final int N = 5, K = 3;
	final static String[] fields = {"ALBUM", "ARTIST", "BPM", "COMMENT", "KEY", "GENRE", "DECIMAL_BPM", "COMMENT 2", "CATALOG_NO", "REMIXER", "PRODUCER", "FOLDER", "FILE", "LABEL", "LYRICS", "MIX", "RANKING", "LOCK"};
	final static String[] ops = {"is","isn't","has","doesn't have",">=","<="};
	private String[][] playlists;
	Scanner sc;
	PrintWriter pw;

	public TraktorAP()
	{
		File c = new File("traktor-autoplaylist.config");
		try
		{
			readConf(new Scanner(c));
			sc = new Scanner(new FileInputStream(conf.get("path") + "collection.nml"), "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();System.exit(0);
			try
			{
				pw = new PrintWriter(new FileWriter(c, false));
				pw.print("path=\nplaylists=\ntrak=");
				pw.close();
				readConf(new Scanner(c));
				sc = new Scanner(new FileInputStream(conf.get("path") + "collection.nml"), "UTF-8");
			}
			catch (IOException x) { log("Can't write config file."); }
		}
		gui();
	}
	//TODO subfolders
	public void gui()
	{
		scanTitles();
		JList listP = new JList(getTitles()), listR = new JList();
		JButton bAdd = new JButton("+"), bDel = new JButton("-"), bEdit = new JButton("Edit"), bStart = new JButton("Start"), bConf = new JButton("Settings");
		JPanel panP = new JPanel(), panR = new JPanel(), panB = new JPanel();
		JScrollPane spanP, spanR;

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int p = listP.getSelectedIndex();
				int r = listR.getSelectedIndex();
				if(p!=-1 && r!=-1)
				{
					if (e.getSource().equals(bDel)) delRule(p, r);
					else if (e.getSource().equals(bEdit)) editRule(p, r);
				}
				if (e.getSource().equals(bAdd)) addRule(p);
				else if(e.getSource().equals(bStart)) start();
				else if(e.getSource().equals(bConf)) editConf();
				listR.setListData(getRules(listP.getSelectedIndex()));
			}
		};
		ListSelectionListener sl = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				listR.setListData(getRules(listP.getSelectedIndex()));
			}
		};

		this.setTitle("Traktor Autoplaylist");
		this.setDefaultCloseOperation(3);
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		listP.setSelectedIndex(0);
		listR.setListData(getRules(0));
		listP.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listR.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listP.addListSelectionListener(sl);

		bAdd.addActionListener(al); bDel.addActionListener(al); bEdit.addActionListener(al);
		bConf.addActionListener(al); bStart.addActionListener(al);

		spanR = new JScrollPane(listR);
		spanR.setPreferredSize(new Dimension(300, 100));
		spanR.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		spanP = new JScrollPane(listP);
		spanP.setPreferredSize(new Dimension(150, 140));
		spanP.setBorder(spanR.getBorder());

		panB.add(bAdd); panB.add(bDel); panB.add(bEdit); panB.add(bConf); panB.add(bStart);
		panR.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Rules"));
		panR.setLayout(new BoxLayout(panR, BoxLayout.Y_AXIS)); panR.add(spanR); panR.add(panB);
		panP.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Playlists"));
		panP.setLayout(new BoxLayout(panP, BoxLayout.Y_AXIS)); panP.add(spanP);

		this.setLayout(new FlowLayout());
		this.add(panP); this.add(panR);
		this.pack();
		this.setVisible(true);
	}

	public void addRule(int p)
	{
		if (p != -1)
		{
			for (int i = 0; i < N; i++)
				playlists[p] = (String[])addTo(playlists[p], "");
			editRule(p, (playlists[p].length-2)/N-1, true);
		}
	}

	public void editRule(int p, int r, boolean... s)
	{
		JPanel j = new JPanel(), box =  new JPanel();
		JComboBox field, op;
		JTextField var;
		JCheckBox cReg = new JCheckBox("Use Regex"), cCase = new JCheckBox("Match case");

		field = new JComboBox(fields);
		op = new JComboBox(ops);
		var = new JTextField();
		var.setPreferredSize(new Dimension(100, 20));

		if(s.length==0)
		{
			cReg.setSelected(Boolean.parseBoolean(playlists[p][2 + N * r]));
			cCase.setSelected(Boolean.parseBoolean(playlists[p][2 + N * r + 1]));
			op.setSelectedIndex(Integer.parseInt(playlists[p][2 + N * r + 3]));
			field.setSelectedIndex(Integer.parseInt(playlists[p][2 + N * r + 2]));
			var.setText(playlists[p][2 + N * r + 4]);
		}

		j.add(field); j.add(op); j.add(var); j.add(cReg); j.add(cCase);

		if(JOptionPane.showConfirmDialog(null, j, "Edit Rule", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
		{
			int f = field.getSelectedIndex();
			int o = op.getSelectedIndex();
			if(!((o==4||o==5||o==7)&&(f!=2))) //w0w much logic
			{
				playlists[p][2 + N * r] = cReg.isSelected() + "";
				playlists[p][2 + N * r + 1] = cCase.isSelected() + "";
				playlists[p][2 + N * r + 2] = field.getSelectedIndex() + "";
				playlists[p][2 + N * r + 3] = op.getSelectedIndex() + "";
				playlists[p][2 + N * r + 4] = var.getText().equals("") ? "0" : var.getText();
			}
			else if(s.length!=0)
				delRule(p, r);
		}
		else if(s.length!=0)
			delRule(p, r);
	}

	public void delRule(int p, int r)
	{
		for (int i = 4; i >= 0; i--)
			playlists[p] = delTo(playlists[p], 2 + N * r + i);
	}

	public void start()
	{
		writeConf();
		new TraktorAP_Process(playlists);
	}

	public String[] getTitles()
	{
		String[] titles = new String[playlists.length];
		for (int i = 0; i < playlists.length; i++)
			titles[i] = playlists[i][0];
		return titles;
	}

	public String[] getRules(int index)
	{
		String[] rules = new String[(playlists[index].length - 2)/N];
		for (int i = 2; i < playlists[index].length; i+=N)
		{
			rules[(i - 2) / N] = fields[Integer.parseInt(playlists[index][i + 2])].toLowerCase() + " "
					+ ops[Integer.parseInt(playlists[index][i + 3])] + " "
					+ "\"" + playlists[index][i + 4] + "\"";
		}
		return rules;
	}

	public void readConf(Scanner sc)
	{
		while (sc.hasNextLine())
		{
			String[] s = sc.nextLine().split("=", 2);
			conf.put(s[0], s[1]);
		}
		String[] s = splitConf("playlists");
		conf.remove("playlists");
		playlists = new String[s.length][];
		for (int i = 0; i < s.length; i++)
			playlists[i] = s[i].split(",", -1);
		if (playlists[0][0].equals(""))
			playlists = new String[0][0];
		else if (!(Arrays.deepToString(playlists).contains(", , true") || Arrays.deepToString(playlists).contains(", , false")))
			for (int i = 0; i < playlists.length; i++)
				for (int k = playlists[i].length - K; k >= K-1; k-=3)
				{
					playlists[i] = addOption(playlists[i], k, "false");
					playlists[i] = addOption(playlists[i], k, "false");
				}
		log(Arrays.deepToString(playlists));
		if (conf.get("path").equals("") || conf.get("trak").equals(""))
			if (JOptionPane.showConfirmDialog(this, "Press OK to enter setup", "Setup", JOptionPane.DEFAULT_OPTION) == JOptionPane.OK_OPTION)
				editConf();
	}

	public void editConf()
	{
		JPanel pan = new JPanel(new GridLayout(4, 1));
		JLabel lPath = new JLabel("Location of collection.nml:"), lTrak = new JLabel("Location of Traktor.app:");
		JButton bPath = new JButton("Choose File..."), bTrak = new JButton("Choose File...");
		JFileChooser fc = new JFileChooser();

		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		String home = fc.getFileSystemView().getDefaultDirectory().toString();
		boolean os = System.getProperty("os.name").contains("Mac");
		if(!conf.get("path").equals(""))
			bPath.setText(conf.get("path"));
		if(!conf.get("trak").equals(""))
			bTrak.setText(conf.get("trak"));
		if(!os)
			lTrak.setText("Location of Traktor.exe:");

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource().equals(bPath))
				{
					if(os) fc.setCurrentDirectory(new File(home + "/Documents/Native Instruments"));
					else fc.setCurrentDirectory(new File(home + "\\Native Instruments"));
					if(fc.showOpenDialog(pan) == JFileChooser.APPROVE_OPTION)
						bPath.setText(fc.getSelectedFile().getAbsolutePath());
				} //TODO file name filter
				else if(e.getSource().equals(bTrak))
				{
					if(os) fc.setCurrentDirectory(new File("/Applications/Native Instruments"));
					else fc.setCurrentDirectory(new File(System.getenv("ProgramFiles") + "\\Native Instruments"));
					if(fc.showOpenDialog(pan) == JFileChooser.APPROVE_OPTION)
						bTrak.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		bPath.addActionListener(al); bTrak.addActionListener(al);
		pan.setPreferredSize(new Dimension(500, 100));
		pan.add(lPath); pan.add(bPath); pan.add(lTrak); pan.add(bTrak);
		if (JOptionPane.showConfirmDialog(null, pan, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
		{
			conf.put("path", bPath.getText().replace("collection.nml", ""));
			conf.put("trak", bTrak.getText());
		}
	}

	public void writeConf()
	{
		try{pw = new PrintWriter(new FileWriter(new File("traktor-autoplaylist.config"), false));}
		catch (IOException e){ log("Write config error"); }
		String p = "playlists=";
		for (int r = 0; r < playlists.length; r++)
		{
			p += playlists[r][0] + ",";
			if(playlists[r].length>2)
				p+=",";
			for (int c = 2; c < playlists[r].length; c++)
			{
				p += playlists[r][c];
				if (c != playlists[r].length - 1)
					p += ",";
			}
			if(r!=playlists.length-1)
				p += ";";
		}
			pw.println(p);
			for (Map.Entry<String, String> entry : conf.entrySet())
			{
				pw.println(entry.getKey() + "=" + entry.getValue());
			}
			pw.close();
		sc.close();
	}

	public Object[] addTo(Object[] r, Object a)
	{
		r = Arrays.copyOf(r, r.length+1);
		r[r.length-1] = a;
		return r;
	}

	public String[] delTo(String[] r, int n)
	{
		List<String> l = new LinkedList();

		for(int i=0; i<r.length; i++)
			if(i!=n)
				l.add(r[i]);

		return l.toArray(new String[l.size()]);
	}

	public String[] addOption(String[] p, int n, String v)
	{
		p = (String[]) addTo(p, "");
		for (int i = p.length - 1; i >= 0; i--)
			if (i >= n)
				p[i] = p[i - 1];
		p[n] = v;
		return p;
	}

	public void scanTitles()
	{
		String[] t = getTitles();
		String a, n="";
		while(sc.hasNextLine())
		{
			a = sc.nextLine();
			if(a.contains("TYPE=\"PLAYLIST\""))
			{
				n = TraktorAP_Process.getVal(a, "TYPE=\"PLAYLIST\" NAME");
				if (!Arrays.toString(t).contains(n))
					playlists = (String[][]) addTo(playlists, new String[]{n, ""});
			}
		}

		/*
		for(int i = 0; i<t.length; i++)
			if(!l.contains(t[i]))
				playlists = (String[][]) delTo(t, i);
				*/

		Arrays.sort(playlists, new java.util.Comparator<String[]>()
		{
			public int compare(String[] a, String[] b)
			{
				return a[0].compareTo(b[0]);
			}
		});
	}

	public void log(Object... k) { for (Object b : k) System.out.print(b.toString() + "\t"); System.out.println();}
	public String[] splitConf(String key){ return conf.get(key).split(";"); }

	public static void main(String[] args) { new TraktorAP(); }
}
//TODO custom objects
/**
 playlists=_LOOPS,;_RECORDINGS,;e1,,6,2,ouse,0,2,1,6,3,prog;e2,,6,2,ouse,0,2,2,6,3,prog;e3,,6,2,ouse,0,2,3,6,3,prog;edm,,6,2,ouse,6,3,prog;t1,,6,2,trance,0,2,1;t2,,6,2,trance,0,2,2;t3,,6,2,trance;trance,,6,2,trance
 path=/Users/Steph/Documents/Native Instruments/Traktor 2.6.8/
 last_run=0/0/0
 trak=/Applications/Native Instruments/Traktor 2/Traktor.app
*/