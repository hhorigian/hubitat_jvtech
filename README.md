# hubitat_jvtech
Driver para integrar com o Leitor LA de Pulso JVTECH.


1. Precisa instalar o Driver.
2. Precisa instalar o APP. No hora de adicionar o código do app na Hubitat, é necessário habilitar o OAuth.

<img width="1621" height="246" alt="image" src="https://github.com/user-attachments/assets/87a14950-8aee-49b4-8bfd-1c14300f09fc" />


3. Adicionar a quantidade desejada Device do tipo Virtual, usando o device JVtech Pulsador, colocar e salvar o numero de serie de cada um. 
4. Adicionar um "User Built-in APP", e adicionar o JVTech Pulse Receiver. Seleccionar os Pulsadores Criados que o APP identifique. Não vai identificar nada se não foram criados no passo #3. Clicar Done. 
5. Entrar no APP JVTech Pulse Receiver, e Pegar o endereço URL que vai aparecer com token, etc.
6. Colocar esse endereço nas configurações do JVTech na parte de envio via HTTP, porta 80. Isso só vai funcionar via LAN na mesma rede.

Cada Leitor, vai ter 4 atributos, com seus respetivos contadores. 
