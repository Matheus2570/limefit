package br.com.limefit.modulos.projeto;

import br.com.limefit.desktop.*;
import javax.swing.*;
import java.awt.*;

/** Apresentação do projeto, integrantes e suporte aos dois idiomas. */
public final class TelaProjeto extends PainelModulo {
    private final JLabel subtitulo=Visual.rotulo();
    private final JLabel[] titulos={Visual.titulo(22),Visual.titulo(22),Visual.titulo(22)};
    private final JTextArea[] textos={Visual.texto(16),Visual.texto(16),Visual.texto(16)};
    private final JButton home=Visual.botao("projeto.home",true);

    public TelaProjeto(ContextoAplicacao contexto) {
        super(contexto,Tela.PROJETO);
        for(JTextArea texto:textos) ((javax.swing.text.DefaultCaret)texto.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
        JPanel raiz=Visual.painel(new BorderLayout(0,18));
        raiz.add(subtitulo,BorderLayout.NORTH);
        JPanel blocos=Visual.painel(new GridBagLayout());
        GridBagConstraints c=new GridBagConstraints(); c.gridx=0; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL;
        for(int i=0;i<3;i++) {
            c.gridy=i*2; c.insets=new Insets(0,0,12,0); blocos.add(titulos[i],c);
            c.gridy++; c.insets=new Insets(0,0,18,0); textos[i].setRows(i==1?6:2); blocos.add(textos[i],c);
        }
        raiz.add(blocos,BorderLayout.CENTER); raiz.add(home,BorderLayout.SOUTH);
        conteudo.add(raiz,BorderLayout.NORTH); home.addActionListener(e -> contexto.navegar(Tela.INICIO));
        atualizarTextos();
    }
    @Override public void atualizarTextos() {
        super.atualizarTextos();
        subtitulo.setText(idioma.escolher("Aplicação desktop em Java Swing com suporte a português e inglês.",
                "Java Swing desktop application with Portuguese and English support."));
        titulos[0].setText(idioma.escolher("Sobre o projeto","About the project"));
        textos[0].setText(idioma.escolher(
                "O LimeFit reúne perfil, alimentos, refeições e planejamento diário. Os registros pertencem à conta autenticada. As porções são informadas em gramas e os totais são calculados a partir dos nutrientes por 100 g.",
                "LimeFit brings together profile, foods, meals and daily planning. Records belong to the signed-in account. Portions are entered in grams and totals are calculated from nutrients per 100 g."));
        titulos[1].setText(idioma.escolher("Equipe e módulos","Team and modules"));
        textos[1].setText(idioma.escolher(
                "• Matheus Maiolo — Perfil\n• Lucas — Alimentos\n• Guilherme — Refeições\n• Gustavo — Planejamento\n• Francisco — Projeto e revisão de inglês\n• Matheus de Carvalho — Home, login e integração",
                "• Matheus Maiolo — Profile\n• Lucas — Foods\n• Guilherme — Meals\n• Gustavo — Planning\n• Francisco — Project and English review\n• Matheus de Carvalho — Home, login and integration"));
        titulos[2].setText(idioma.escolher("Português e inglês","Portuguese and English"));
        textos[2].setText(idioma.escolher(
                "Use PT/EN no cabeçalho para alternar os textos, campos, validações e mensagens. O idioma inicial é inglês. Perfil, alimentos, refeições e planejamento compartilham a mesma sessão.",
                "Use PT/EN in the header to switch labels, fields, validation and messages. The initial language is English. Profile, foods, meals and planning share the same session."));
        home.setText(idioma.escolher("Ir para Home","Go to Home"));
    }
}
