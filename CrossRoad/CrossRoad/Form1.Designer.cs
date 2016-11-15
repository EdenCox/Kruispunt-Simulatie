namespace CrossRoad
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.buttonStartListener = new System.Windows.Forms.Button();
            this.buttonStopListener = new System.Windows.Forms.Button();
            this.listViewClients = new System.Windows.Forms.ListView();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // buttonStartListener
            // 
            this.buttonStartListener.Location = new System.Drawing.Point(207, 12);
            this.buttonStartListener.Name = "buttonStartListener";
            this.buttonStartListener.Size = new System.Drawing.Size(75, 23);
            this.buttonStartListener.TabIndex = 0;
            this.buttonStartListener.Text = "start listener";
            this.buttonStartListener.UseVisualStyleBackColor = true;
            this.buttonStartListener.Click += new System.EventHandler(this.buttonStartListener_Click);
            // 
            // buttonStopListener
            // 
            this.buttonStopListener.Location = new System.Drawing.Point(207, 41);
            this.buttonStopListener.Name = "buttonStopListener";
            this.buttonStopListener.Size = new System.Drawing.Size(75, 23);
            this.buttonStopListener.TabIndex = 1;
            this.buttonStopListener.Text = "stop listener";
            this.buttonStopListener.UseVisualStyleBackColor = true;
            this.buttonStopListener.Click += new System.EventHandler(this.buttonStopListener_Click);
            // 
            // listViewClients
            // 
            this.listViewClients.Location = new System.Drawing.Point(12, 72);
            this.listViewClients.Name = "listViewClients";
            this.listViewClients.Size = new System.Drawing.Size(189, 78);
            this.listViewClients.TabIndex = 2;
            this.listViewClients.UseCompatibleStateImageBehavior = false;
            // 
            // textBoxPort
            // 
            this.textBoxPort.Location = new System.Drawing.Point(57, 14);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(100, 20);
            this.textBoxPort.TabIndex = 3;
            this.textBoxPort.Text = "8080";
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(22, 21);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(29, 13);
            this.label1.TabIndex = 4;
            this.label1.Text = "Port:";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.textBoxPort);
            this.Controls.Add(this.listViewClients);
            this.Controls.Add(this.buttonStopListener);
            this.Controls.Add(this.buttonStartListener);
            this.Name = "Form1";
            this.Text = "Form1";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button buttonStartListener;
        private System.Windows.Forms.Button buttonStopListener;
        private System.Windows.Forms.ListView listViewClients;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.Label label1;
    }
}

