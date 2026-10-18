# -*- coding: utf-8 -*-
"""Terminal: runs Python 2.7 code inside Skylight OS. The variable app is this
app, so app.postNotification('Hi') and the rest of the App API work."""
import ast
import sys
import traceback
import Tkinter as tk
import ttk
from StringIO import StringIO

from skylight.app import App


class TerminalApp(App):

    def build(self):
        toolbar = ttk.Frame(self.window, padding=4)
        toolbar.pack(side='top', fill='x')
        ttk.Button(toolbar, text='Run', command=self.run).pack(side='left')
        ttk.Button(toolbar, text='Clear output', command=lambda: self.output.delete('1.0', 'end')).pack(side='left', padx=4)
        ttk.Label(toolbar, text='Python 2.7. app is this app.').pack(side='right')

        panes = tk.PanedWindow(self.window, orient='vertical', sashwidth=6, borderwidth=0)
        panes.pack(fill='both', expand=True)
        self.code = tk.Text(panes, background='#000000', foreground='#ffffff', insertbackground='#ffffff',
                            font=('Courier', 10), undo=True, wrap='none', height=12)
        self.output = tk.Text(panes, background='#1e1e1e', foreground='#9fe39f',
                              font=('Courier', 10), wrap='none', height=6)
        self.output.bind('<Key>', lambda event: None if event.state & 4 else 'break')   # read-only, but copyable
        panes.add(self.code, stretch='always')
        panes.add(self.output, stretch='always')
        self.namespace = {'app': self}   # kept between runs, like an interactive Python
        self.code.focus_set()

    def run(self):
        """Runs the code and shows what it printed, the value of its last line, or the error."""
        source = self.code.get('1.0', 'end-1c')
        printed = StringIO()
        saved = sys.stdout
        sys.stdout = printed
        result = None
        try:
            tree = ast.parse(source, '<terminal>')
            last = None
            if tree.body and isinstance(tree.body[-1], ast.Expr):
                last = ast.Expression(tree.body.pop().value)
            exec compile(tree, '<terminal>', 'exec', 0, True) in self.namespace
            if last is not None:
                result = eval(compile(last, '<terminal>', 'eval', 0, True), self.namespace)
        except SyntaxError as error:
            print 'SyntaxError: %s (line %s)' % (error.msg, error.lineno)
        except Exception as error:
            print traceback.format_exception_only(type(error), error)[-1].strip()
        finally:
            sys.stdout = saved
        text = printed.getvalue()
        if result is not None:
            text += '=> %r\n' % (result,)
        self.output.insert('end', text or '(done)\n')
        self.output.see('end')
