package com.projeto.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

public class jogo extends ApplicationAdapter {

	// usado para renderizar imagens/formas dentro do jogo
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	// formas para colisão
	private ShapeRenderer shapeRenderer; // p exibir as formas
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	// atributos p configurações
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 0;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos = 400;
	private Random random;
	private int pontos = 0;
	private boolean passouCano;
	private int estadoJogo = 0;

	// textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont melhorPotuacao;
	
	@Override
	public void create () {

		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {

		verificaEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

	}
	
	@Override
	public void dispose () {

	}

	private void verificaEstadoJogo(){

		// evento de clique
		boolean toqueTela = Gdx.input.justTouched();


		if( estadoJogo == 0){

			if( toqueTela ) {
				gravidade = -20;
				estadoJogo = 1;
			}

		}else if( estadoJogo == 1){

			if( toqueTela ) {
				gravidade = -20;
			}

			// movimentar o cano
			posicaoCanoHorizontal -=Gdx.graphics.getDeltaTime() * 200;
			if( posicaoCanoHorizontal < -canoTopo.getWidth() ){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(600) - 300;
				passouCano = false;
			}

			// aplicar gravidade no pássaro
			if( posicaoInicialVerticalPassaro > 0 || toqueTela )
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;

		}else if( estadoJogo == 2){

		}


	}

	private void detectarColisoes(){

		circuloPassaro.set(
				50 + passaros[0].getWidth()/2, posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2
		);
		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		// testar se o pássaro se sobrepos aos retangulos
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps( circuloPassaro, retanguloCanoBaixo);

		if( colidiuCanoCima || colidiuCanoBaixo){
			estadoJogo = 2;
		}

		/*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled); // usado para desenhar formas geométricas

		shapeRenderer.circle(50 + passaros[0].getWidth()/2, posicaoInicialVerticalPassaro + passaros[0].getHeight()/2, passaros[0].getWidth()/2);
		// topo
		shapeRenderer.rect(
				posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		// baixo
		shapeRenderer.rect(
				posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		shapeRenderer.end();*/
	}

	private void desenharTexturas(){

		batch.begin(); // exibir imagem/forma dentro do app

		// desenha imagem dentro do jogo
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo );
		batch.draw( passaros[(int) /*arredonda*/ variacao], 50, posicaoInicialVerticalPassaro );
		batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf( pontos ), larguraDispositivo/2, alturaDispositivo - 100 );

		if( estadoJogo == 2 ){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo/2 - 140 , alturaDispositivo/2 - gameOver.getHeight()/2);
			melhorPotuacao.draw(batch, "Seu record é", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight() );
		}

		batch.end(); // identificar que é o final do desenho no app

	}

	private void inicializarTexturas(){

		passaros 	= new Texture[3];
		passaros[0] = new Texture("passaro1.png"); // instanciando o passaro
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo 			= new Texture("fundo.png");
		canoBaixo 		= new Texture("cano_baixo_maior.png");
		canoTopo		= new Texture("cano_topo_maior.png");
		gameOver		= new Texture("game_over.png");
	}

	private void inicializarObjetos(){

		batch 	= new SpriteBatch(); // instanciando o Batch
		random	= new Random();

		larguraDispositivo =  Gdx.graphics.getWidth();
		alturaDispositivo = Gdx.graphics.getHeight();
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal = larguraDispositivo;

		// textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		melhorPotuacao = new BitmapFont();
		melhorPotuacao.setColor(Color.RED);
		melhorPotuacao.getData().setScale(2);

		// formas de colisões
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		shapeRenderer = new ShapeRenderer();
	}

	private void validarPontos(){

		if( posicaoCanoHorizontal < 30 ){ // passou do passaro
			if(!passouCano) {
				pontos++;
				passouCano = true;
			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;
		if (variacao > 3)
			variacao = 0;
	}
}
