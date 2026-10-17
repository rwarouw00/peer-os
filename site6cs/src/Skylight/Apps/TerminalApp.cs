using System;
using System.CodeDom.Compiler;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Windows.Forms;
using Microsoft.CSharp;

namespace Skylight.Apps
{
	/// <summary>
	/// Terminal: runs C# statements, compiled on the spot by the same compiler
	/// that built Skylight. The variable app is this app, so
	/// app.PostNotification("Hi") and the rest of the App API work.
	/// </summary>
	public class TerminalApp : App
	{
		readonly TextBox code = new TextBox();
		readonly TextBox output = new TextBox();

		protected internal override Control Build()
		{
			foreach (TextBox box in new[] { code, output })
			{
				box.Multiline = true;
				box.ScrollBars = ScrollBars.Both;
				box.WordWrap = false;
				box.Dock = DockStyle.Fill;
				box.Font = new Font(FontFamily.GenericMonospace, 10);
				box.AcceptsTab = true;
			}
			code.BackColor = Color.Black;
			code.ForeColor = Color.White;
			output.BackColor = Color.FromArgb(30, 30, 30);
			output.ForeColor = Color.FromArgb(159, 227, 159);
			output.ReadOnly = true;

			ToolStrip toolbar = new ToolStrip { GripStyle = ToolStripGripStyle.Hidden };
			toolbar.Items.Add("Run", null, delegate { Run(); });
			toolbar.Items.Add("Clear output", null, delegate { output.Clear(); });
			toolbar.Items.Add(new ToolStripLabel("C# statements. app is this app; print(x) shows x.") { Alignment = ToolStripItemAlignment.Right });

			SplitContainer panes = new SplitContainer { Orientation = Orientation.Horizontal, Dock = DockStyle.Fill, SplitterDistance = 220 };
			panes.Panel1.Controls.Add(code);
			panes.Panel2.Controls.Add(output);

			Panel root = new Panel();
			root.Controls.Add(panes);
			root.Controls.Add(toolbar);
			return root;
		}

		/// <summary>Wraps the code in a method, compiles it, runs it and shows what it printed and returned.</summary>
		void Run()
		{
			string source =
				"using System; using System.Collections.Generic; using System.IO; using System.Linq; using Skylight;\n" +
				"public static class Snippet {\n" +
				"  static TextWriter output;\n" +
				"  static void print(object value) { output.WriteLine(value); }\n" +
				"  public static object Run(App app, TextWriter writer) { output = writer;\n" +
				"#line 1\n" + code.Text + "\n" +
				"  return null; }\n}\n";

			CompilerParameters options = new CompilerParameters { GenerateInMemory = true };
			options.ReferencedAssemblies.AddRange(new[] { "System.dll", "System.Core.dll", "System.Drawing.dll", "System.Windows.Forms.dll" });
			options.ReferencedAssemblies.Add(Assembly.GetExecutingAssembly().Location);
			CompilerResults compiled;
			using (CSharpCodeProvider compiler = new CSharpCodeProvider())
			{
				compiled = compiler.CompileAssemblyFromSource(options, source);
			}
			if (compiled.Errors.HasErrors)
			{
				foreach (CompilerError error in compiled.Errors.Cast<CompilerError>().Where(e => !e.IsWarning))
				{
					Append("Line " + error.Line + ": " + error.ErrorText);
				}
				return;
			}

			StringWriter printed = new StringWriter();
			try
			{
				object result = compiled.CompiledAssembly.GetType("Snippet").GetMethod("Run").Invoke(null, new object[] { this, printed });
				Append(printed + (result == null ? "" : "=> " + result + Environment.NewLine));
			}
			catch (TargetInvocationException e)
			{
				Append(printed + "Error: " + e.InnerException.GetType().Name + ": " + e.InnerException.Message);
			}
		}

		void Append(string text)
		{
			text = text.TrimEnd('\r', '\n');
			output.AppendText((text.Length == 0 ? "(done)" : text).Replace("\r\n", "\n").Replace("\n", Environment.NewLine) + Environment.NewLine);
		}
	}
}
