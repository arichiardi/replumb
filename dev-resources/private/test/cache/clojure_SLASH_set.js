// Compiled by ClojureScript 1.7.228
goog.provide("clojure.set");
(function (){
clojure.set.bubble_max_key = (function clojure$set$bubble_max_key(k,coll){

var max = cljs.core.apply.call(null,cljs.core.max_key,k,coll);
return cljs.core.cons.call(null,max,cljs.core.remove.call(null,((function (max){
return (function (p1__1_SHARP_){
return (max === p1__1_SHARP_);
});})(max))
,coll));
}); return (
new cljs.core.Var(function(){return clojure.set.bubble_max_key;},new cljs.core.Symbol("clojure.set","bubble-max-key","clojure.set/bubble-max-key",(1311592486),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"private","private",(-558947994)),new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[true,cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"bubble-max-key","bubble-max-key",(1810783950),null),null,(22),(1),(13),(13),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"k","k",(-505765866),null),new cljs.core.Symbol(null,"coll","coll",(-1006698606),null)], null)),null,(cljs.core.truth_(clojure.set.bubble_max_key)?clojure.set.bubble_max_key.cljs$lang$test:null)])));})()
;
/**
 * Return a set that is the union of the input sets
 */
(function (){
clojure.set.union = (function clojure$set$union(var_args){
var args22 = [];
var len__15229__auto___28 = arguments.length;
var i__15230__auto___29 = (0);
while(true){
if((i__15230__auto___29 < len__15229__auto___28)){
args22.push((arguments[i__15230__auto___29]));

var G__30 = (i__15230__auto___29 + (1));
i__15230__auto___29 = G__30;
continue;
} else {
}
break;
}

var G__27 = args22.length;
switch (G__27) {
case (0):
return clojure.set.union.cljs$core$IFn$_invoke$arity$0();

break;
case (1):
return clojure.set.union.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case (2):
return clojure.set.union.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
var argseq__15240__auto__ = (new cljs.core.IndexedSeq(args22.slice((2)),(0)));
return clojure.set.union.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__15240__auto__);

}
}); return (
new cljs.core.Var(function(){return clojure.set.union;},new cljs.core.Symbol("clojure.set","union","clojure.set/union",(-71291846),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"top-fn","top-fn",(-2056129173)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"union","union",(-511498270),null),null,(12),new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"variadic","variadic",(882626057)),true,new cljs.core.Keyword(null,"max-fixed-arity","max-fixed-arity",(-690205543)),(2),new cljs.core.Keyword(null,"method-params","method-params",(-980792179)),cljs.core.list(cljs.core.PersistentVector.EMPTY,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),cljs.core.list(cljs.core.PersistentVector.EMPTY,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),new cljs.core.Keyword(null,"arglists-meta","arglists-meta",(1944829838)),cljs.core.list(null,null,null,null)], null),(1),(19),(19),cljs.core.list(cljs.core.PersistentVector.EMPTY,new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),"Return a set that is the union of the input sets",(cljs.core.truth_(clojure.set.union)?clojure.set.union.cljs$lang$test:null)])));})()
;

clojure.set.union.cljs$core$IFn$_invoke$arity$0 = (function (){
return cljs.core.PersistentHashSet.EMPTY;
});

clojure.set.union.cljs$core$IFn$_invoke$arity$1 = (function (s1){
return s1;
});

clojure.set.union.cljs$core$IFn$_invoke$arity$2 = (function (s1,s2){
if((cljs.core.count.call(null,s1) < cljs.core.count.call(null,s2))){
return cljs.core.reduce.call(null,cljs.core.conj,s2,s1);
} else {
return cljs.core.reduce.call(null,cljs.core.conj,s1,s2);
}
});

clojure.set.union.cljs$core$IFn$_invoke$arity$variadic = (function (s1,s2,sets){
var bubbled_sets = clojure.set.bubble_max_key.call(null,cljs.core.count,cljs.core.conj.call(null,sets,s2,s1));
return cljs.core.reduce.call(null,cljs.core.into,cljs.core.first.call(null,bubbled_sets),cljs.core.rest.call(null,bubbled_sets));
});

clojure.set.union.cljs$lang$applyTo = (function (seq23){
var G__24 = cljs.core.first.call(null,seq23);
var seq23__$1 = cljs.core.next.call(null,seq23);
var G__25 = cljs.core.first.call(null,seq23__$1);
var seq23__$2 = cljs.core.next.call(null,seq23__$1);
return clojure.set.union.cljs$core$IFn$_invoke$arity$variadic(G__24,G__25,seq23__$2);
});

clojure.set.union.cljs$lang$maxFixedArity = (2);
/**
 * Return a set that is the intersection of the input sets
 */
(function (){
clojure.set.intersection = (function clojure$set$intersection(var_args){
var args32 = [];
var len__15229__auto___38 = arguments.length;
var i__15230__auto___39 = (0);
while(true){
if((i__15230__auto___39 < len__15229__auto___38)){
args32.push((arguments[i__15230__auto___39]));

var G__40 = (i__15230__auto___39 + (1));
i__15230__auto___39 = G__40;
continue;
} else {
}
break;
}

var G__37 = args32.length;
switch (G__37) {
case (1):
return clojure.set.intersection.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case (2):
return clojure.set.intersection.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
var argseq__15240__auto__ = (new cljs.core.IndexedSeq(args32.slice((2)),(0)));
return clojure.set.intersection.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__15240__auto__);

}
}); return (
new cljs.core.Var(function(){return clojure.set.intersection;},new cljs.core.Symbol("clojure.set","intersection","clojure.set/intersection",(-1478098847),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"top-fn","top-fn",(-2056129173)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"intersection","intersection",(-650544759),null),null,(19),new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"variadic","variadic",(882626057)),true,new cljs.core.Keyword(null,"max-fixed-arity","max-fixed-arity",(-690205543)),(2),new cljs.core.Keyword(null,"method-params","method-params",(-980792179)),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),new cljs.core.Keyword(null,"arglists-meta","arglists-meta",(1944829838)),cljs.core.list(null,null,null)], null),(1),(31),(31),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),"Return a set that is the intersection of the input sets",(cljs.core.truth_(clojure.set.intersection)?clojure.set.intersection.cljs$lang$test:null)])));})()
;

clojure.set.intersection.cljs$core$IFn$_invoke$arity$1 = (function (s1){
return s1;
});

clojure.set.intersection.cljs$core$IFn$_invoke$arity$2 = (function (s1,s2){
while(true){
if((cljs.core.count.call(null,s2) < cljs.core.count.call(null,s1))){
var G__42 = s2;
var G__43 = s1;
s1 = G__42;
s2 = G__43;
continue;
} else {
return cljs.core.reduce.call(null,((function (s1,s2){
return (function (result,item){
if(cljs.core.contains_QMARK_.call(null,s2,item)){
return result;
} else {
return cljs.core.disj.call(null,result,item);
}
});})(s1,s2))
,s1,s1);
}
break;
}
});

clojure.set.intersection.cljs$core$IFn$_invoke$arity$variadic = (function (s1,s2,sets){
var bubbled_sets = clojure.set.bubble_max_key.call(null,(function (p1__2_SHARP_){
return (- cljs.core.count.call(null,p1__2_SHARP_));
}),cljs.core.conj.call(null,sets,s2,s1));
return cljs.core.reduce.call(null,clojure.set.intersection,cljs.core.first.call(null,bubbled_sets),cljs.core.rest.call(null,bubbled_sets));
});

clojure.set.intersection.cljs$lang$applyTo = (function (seq33){
var G__34 = cljs.core.first.call(null,seq33);
var seq33__$1 = cljs.core.next.call(null,seq33);
var G__35 = cljs.core.first.call(null,seq33__$1);
var seq33__$2 = cljs.core.next.call(null,seq33__$1);
return clojure.set.intersection.cljs$core$IFn$_invoke$arity$variadic(G__34,G__35,seq33__$2);
});

clojure.set.intersection.cljs$lang$maxFixedArity = (2);
/**
 * Return a set that is the first set without elements of the remaining sets
 */
(function (){
clojure.set.difference = (function clojure$set$difference(var_args){
var args44 = [];
var len__15229__auto___50 = arguments.length;
var i__15230__auto___51 = (0);
while(true){
if((i__15230__auto___51 < len__15229__auto___50)){
args44.push((arguments[i__15230__auto___51]));

var G__52 = (i__15230__auto___51 + (1));
i__15230__auto___51 = G__52;
continue;
} else {
}
break;
}

var G__49 = args44.length;
switch (G__49) {
case (1):
return clojure.set.difference.cljs$core$IFn$_invoke$arity$1((arguments[(0)]));

break;
case (2):
return clojure.set.difference.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
default:
var argseq__15240__auto__ = (new cljs.core.IndexedSeq(args44.slice((2)),(0)));
return clojure.set.difference.cljs$core$IFn$_invoke$arity$variadic((arguments[(0)]),(arguments[(1)]),argseq__15240__auto__);

}
}); return (
new cljs.core.Var(function(){return clojure.set.difference;},new cljs.core.Symbol("clojure.set","difference","clojure.set/difference",(-1178931405),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"top-fn","top-fn",(-2056129173)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"difference","difference",(-738334373),null),null,(17),new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"variadic","variadic",(882626057)),true,new cljs.core.Keyword(null,"max-fixed-arity","max-fixed-arity",(-690205543)),(2),new cljs.core.Keyword(null,"method-params","method-params",(-980792179)),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),new cljs.core.Keyword(null,"arglists-meta","arglists-meta",(1944829838)),cljs.core.list(null,null,null)], null),(1),(46),(46),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null)], null),new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null)], null),new cljs.core.PersistentVector(null, 4, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"s1","s1",(338671490),null),new cljs.core.Symbol(null,"s2","s2",(614604262),null),new cljs.core.Symbol(null,"&","&",(-2144855648),null),new cljs.core.Symbol(null,"sets","sets",(2041487109),null)], null)),"Return a set that is the first set without elements of the remaining sets",(cljs.core.truth_(clojure.set.difference)?clojure.set.difference.cljs$lang$test:null)])));})()
;

clojure.set.difference.cljs$core$IFn$_invoke$arity$1 = (function (s1){
return s1;
});

clojure.set.difference.cljs$core$IFn$_invoke$arity$2 = (function (s1,s2){
if((cljs.core.count.call(null,s1) < cljs.core.count.call(null,s2))){
return cljs.core.reduce.call(null,(function (result,item){
if(cljs.core.contains_QMARK_.call(null,s2,item)){
return cljs.core.disj.call(null,result,item);
} else {
return result;
}
}),s1,s1);
} else {
return cljs.core.reduce.call(null,cljs.core.disj,s1,s2);
}
});

clojure.set.difference.cljs$core$IFn$_invoke$arity$variadic = (function (s1,s2,sets){
return cljs.core.reduce.call(null,clojure.set.difference,s1,cljs.core.conj.call(null,sets,s2));
});

clojure.set.difference.cljs$lang$applyTo = (function (seq45){
var G__46 = cljs.core.first.call(null,seq45);
var seq45__$1 = cljs.core.next.call(null,seq45);
var G__47 = cljs.core.first.call(null,seq45__$1);
var seq45__$2 = cljs.core.next.call(null,seq45__$1);
return clojure.set.difference.cljs$core$IFn$_invoke$arity$variadic(G__46,G__47,seq45__$2);
});

clojure.set.difference.cljs$lang$maxFixedArity = (2);
/**
 * Returns a set of the elements for which pred is true
 */
(function (){
clojure.set.select = (function clojure$set$select(pred,xset){
return cljs.core.reduce.call(null,(function (s,k){
if(cljs.core.truth_(pred.call(null,k))){
return s;
} else {
return cljs.core.disj.call(null,s,k);
}
}),xset,xset);
}); return (
new cljs.core.Var(function(){return clojure.set.select;},new cljs.core.Symbol("clojure.set","select","clojure.set/select",(-2081547970),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"select","select",(-1506602266),null),null,(13),(1),(61),(61),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"pred","pred",(-727012372),null),new cljs.core.Symbol(null,"xset","xset",(-371743149),null)], null)),"Returns a set of the elements for which pred is true",(cljs.core.truth_(clojure.set.select)?clojure.set.select.cljs$lang$test:null)])));})()
;
/**
 * Returns a rel of the elements of xrel with only the keys in ks
 */
(function (){
clojure.set.project = (function clojure$set$project(xrel,ks){
return cljs.core.set.call(null,cljs.core.map.call(null,(function (p1__3_SHARP_){
return cljs.core.select_keys.call(null,p1__3_SHARP_,ks);
}),xrel));
}); return (
new cljs.core.Var(function(){return clojure.set.project;},new cljs.core.Symbol("clojure.set","project","clojure.set/project",(-829527518),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"project","project",(-1530041190),null),null,(14),(1),(67),(67),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"ks","ks",(-754231827),null)], null)),"Returns a rel of the elements of xrel with only the keys in ks",(cljs.core.truth_(clojure.set.project)?clojure.set.project.cljs$lang$test:null)])));})()
;
/**
 * Returns the map with the keys in kmap renamed to the vals in kmap
 */
(function (){
clojure.set.rename_keys = (function clojure$set$rename_keys(map,kmap){
return cljs.core.reduce.call(null,(function (m,p__56){
var vec__57 = p__56;
var old = cljs.core.nth.call(null,vec__57,(0),null);
var new$ = cljs.core.nth.call(null,vec__57,(1),null);
if(cljs.core.contains_QMARK_.call(null,map,old)){
return cljs.core.assoc.call(null,m,new$,cljs.core.get.call(null,map,old));
} else {
return m;
}
}),cljs.core.apply.call(null,cljs.core.dissoc,map,cljs.core.keys.call(null,kmap)),kmap);
}); return (
new cljs.core.Var(function(){return clojure.set.rename_keys;},new cljs.core.Symbol("clojure.set","rename-keys","clojure.set/rename-keys",(996223920),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"rename-keys","rename-keys",(355340888),null),null,(18),(1),(72),(72),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"map","map",(-1282745308),null),new cljs.core.Symbol(null,"kmap","kmap",(-2108296910),null)], null)),"Returns the map with the keys in kmap renamed to the vals in kmap",(cljs.core.truth_(clojure.set.rename_keys)?clojure.set.rename_keys.cljs$lang$test:null)])));})()
;
/**
 * Returns a rel of the maps in xrel with the keys in kmap renamed to the vals in kmap
 */
(function (){
clojure.set.rename = (function clojure$set$rename(xrel,kmap){
return cljs.core.set.call(null,cljs.core.map.call(null,(function (p1__4_SHARP_){
return clojure.set.rename_keys.call(null,p1__4_SHARP_,kmap);
}),xrel));
}); return (
new cljs.core.Var(function(){return clojure.set.rename;},new cljs.core.Symbol("clojure.set","rename","clojure.set/rename",(-1779816356),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"rename","rename",(-1146278156),null),null,(13),(1),(82),(82),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"kmap","kmap",(-2108296910),null)], null)),"Returns a rel of the maps in xrel with the keys in kmap renamed to the vals in kmap",(cljs.core.truth_(clojure.set.rename)?clojure.set.rename.cljs$lang$test:null)])));})()
;
/**
 * Returns a map of the distinct values of ks in the xrel mapped to a
 *   set of the maps in xrel with the corresponding values of ks.
 */
(function (){
clojure.set.index = (function clojure$set$index(xrel,ks){
return cljs.core.reduce.call(null,(function (m,x){
var ik = cljs.core.select_keys.call(null,x,ks);
return cljs.core.assoc.call(null,m,ik,cljs.core.conj.call(null,cljs.core.get.call(null,m,ik,cljs.core.PersistentHashSet.EMPTY),x));
}),cljs.core.PersistentArrayMap.EMPTY,xrel);
}); return (
new cljs.core.Var(function(){return clojure.set.index;},new cljs.core.Symbol("clojure.set","index","clojure.set/index",(1009901700),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"index","index",(108845612),null),null,(12),(1),(87),(87),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"ks","ks",(-754231827),null)], null)),"Returns a map of the distinct values of ks in the xrel mapped to a\n  set of the maps in xrel with the corresponding values of ks.",(cljs.core.truth_(clojure.set.index)?clojure.set.index.cljs$lang$test:null)])));})()
;
/**
 * Returns the map with the vals mapped to the keys.
 */
(function (){
clojure.set.map_invert = (function clojure$set$map_invert(m){
return cljs.core.reduce.call(null,(function (m__$1,p__60){
var vec__61 = p__60;
var k = cljs.core.nth.call(null,vec__61,(0),null);
var v = cljs.core.nth.call(null,vec__61,(1),null);
return cljs.core.assoc.call(null,m__$1,v,k);
}),cljs.core.PersistentArrayMap.EMPTY,m);
}); return (
new cljs.core.Var(function(){return clojure.set.map_invert;},new cljs.core.Symbol("clojure.set","map-invert","clojure.set/map-invert",(1632506396),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"map-invert","map-invert",(1258886340),null),null,(17),(1),(97),(97),cljs.core.list(new cljs.core.PersistentVector(null, 1, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"m","m",(-1021758608),null)], null)),"Returns the map with the vals mapped to the keys.",(cljs.core.truth_(clojure.set.map_invert)?clojure.set.map_invert.cljs$lang$test:null)])));})()
;
/**
 * When passed 2 rels, returns the rel corresponding to the natural
 *   join. When passed an additional keymap, joins on the corresponding
 *   keys.
 */
(function (){
clojure.set.join = (function clojure$set$join(var_args){
var args62 = [];
var len__15229__auto___67 = arguments.length;
var i__15230__auto___68 = (0);
while(true){
if((i__15230__auto___68 < len__15229__auto___67)){
args62.push((arguments[i__15230__auto___68]));

var G__69 = (i__15230__auto___68 + (1));
i__15230__auto___68 = G__69;
continue;
} else {
}
break;
}

var G__64 = args62.length;
switch (G__64) {
case (2):
return clojure.set.join.cljs$core$IFn$_invoke$arity$2((arguments[(0)]),(arguments[(1)]));

break;
case (3):
return clojure.set.join.cljs$core$IFn$_invoke$arity$3((arguments[(0)]),(arguments[(1)]),(arguments[(2)]));

break;
default:
throw (new Error([cljs.core.str("Invalid arity: "),cljs.core.str(args62.length)].join('')));

}
}); return (
new cljs.core.Var(function(){return clojure.set.join;},new cljs.core.Symbol("clojure.set","join","clojure.set/join",(-621789763),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"top-fn","top-fn",(-2056129173)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"join","join",(881669637),null),null,(11),new cljs.core.PersistentArrayMap(null, 5, [new cljs.core.Keyword(null,"variadic","variadic",(882626057)),false,new cljs.core.Keyword(null,"max-fixed-arity","max-fixed-arity",(-690205543)),(3),new cljs.core.Keyword(null,"method-params","method-params",(-980792179)),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null),new cljs.core.Symbol(null,"km","km",(-1276648257),null)], null)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null),new cljs.core.Symbol(null,"km","km",(-1276648257),null)], null)),new cljs.core.Keyword(null,"arglists-meta","arglists-meta",(1944829838)),cljs.core.list(null,null)], null),(1),(101),(101),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null)], null),new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"xrel","xrel",(-735800081),null),new cljs.core.Symbol(null,"yrel","yrel",(-1573821256),null),new cljs.core.Symbol(null,"km","km",(-1276648257),null)], null)),"When passed 2 rels, returns the rel corresponding to the natural\n  join. When passed an additional keymap, joins on the corresponding\n  keys.",(cljs.core.truth_(clojure.set.join)?clojure.set.join.cljs$lang$test:null)])));})()
;

clojure.set.join.cljs$core$IFn$_invoke$arity$2 = (function (xrel,yrel){
if((cljs.core.seq.call(null,xrel)) && (cljs.core.seq.call(null,yrel))){
var ks = clojure.set.intersection.call(null,cljs.core.set.call(null,cljs.core.keys.call(null,cljs.core.first.call(null,xrel))),cljs.core.set.call(null,cljs.core.keys.call(null,cljs.core.first.call(null,yrel))));
var vec__65 = (((cljs.core.count.call(null,xrel) <= cljs.core.count.call(null,yrel)))?new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [xrel,yrel], null):new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [yrel,xrel], null));
var r = cljs.core.nth.call(null,vec__65,(0),null);
var s = cljs.core.nth.call(null,vec__65,(1),null);
var idx = clojure.set.index.call(null,r,ks);
return cljs.core.reduce.call(null,((function (ks,vec__65,r,s,idx){
return (function (ret,x){
var found = idx.call(null,cljs.core.select_keys.call(null,x,ks));
if(cljs.core.truth_(found)){
return cljs.core.reduce.call(null,((function (found,ks,vec__65,r,s,idx){
return (function (p1__5_SHARP_,p2__6_SHARP_){
return cljs.core.conj.call(null,p1__5_SHARP_,cljs.core.merge.call(null,p2__6_SHARP_,x));
});})(found,ks,vec__65,r,s,idx))
,ret,found);
} else {
return ret;
}
});})(ks,vec__65,r,s,idx))
,cljs.core.PersistentHashSet.EMPTY,s);
} else {
return cljs.core.PersistentHashSet.EMPTY;
}
});

clojure.set.join.cljs$core$IFn$_invoke$arity$3 = (function (xrel,yrel,km){
var vec__66 = (((cljs.core.count.call(null,xrel) <= cljs.core.count.call(null,yrel)))?new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [xrel,yrel,clojure.set.map_invert.call(null,km)], null):new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [yrel,xrel,km], null));
var r = cljs.core.nth.call(null,vec__66,(0),null);
var s = cljs.core.nth.call(null,vec__66,(1),null);
var k = cljs.core.nth.call(null,vec__66,(2),null);
var idx = clojure.set.index.call(null,r,cljs.core.vals.call(null,k));
return cljs.core.reduce.call(null,((function (vec__66,r,s,k,idx){
return (function (ret,x){
var found = idx.call(null,clojure.set.rename_keys.call(null,cljs.core.select_keys.call(null,x,cljs.core.keys.call(null,k)),k));
if(cljs.core.truth_(found)){
return cljs.core.reduce.call(null,((function (found,vec__66,r,s,k,idx){
return (function (p1__7_SHARP_,p2__8_SHARP_){
return cljs.core.conj.call(null,p1__7_SHARP_,cljs.core.merge.call(null,p2__8_SHARP_,x));
});})(found,vec__66,r,s,k,idx))
,ret,found);
} else {
return ret;
}
});})(vec__66,r,s,k,idx))
,cljs.core.PersistentHashSet.EMPTY,s);
});

clojure.set.join.cljs$lang$maxFixedArity = (3);
/**
 * Is set1 a subset of set2?
 */
(function (){
clojure.set.subset_QMARK_ = (function clojure$set$subset_QMARK_(set1,set2){
return ((cljs.core.count.call(null,set1) <= cljs.core.count.call(null,set2))) && (cljs.core.every_QMARK_.call(null,(function (p1__9_SHARP_){
return cljs.core.contains_QMARK_.call(null,set2,p1__9_SHARP_);
}),set1));
}); return (
new cljs.core.Var(function(){return clojure.set.subset_QMARK_;},new cljs.core.Symbol("clojure.set","subset?","clojure.set/subset?",(909155479),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"subset?","subset?",(1551079215),null),null,(14),(1),(131),(131),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"set1","set1",(-1952562536),null),new cljs.core.Symbol(null,"set2","set2",(1231516531),null)], null)),"Is set1 a subset of set2?",(cljs.core.truth_(clojure.set.subset_QMARK_)?clojure.set.subset_QMARK_.cljs$lang$test:null)])));})()
;
/**
 * Is set1 a superset of set2?
 */
(function (){
clojure.set.superset_QMARK_ = (function clojure$set$superset_QMARK_(set1,set2){
return ((cljs.core.count.call(null,set1) >= cljs.core.count.call(null,set2))) && (cljs.core.every_QMARK_.call(null,(function (p1__10_SHARP_){
return cljs.core.contains_QMARK_.call(null,set1,p1__10_SHARP_);
}),set2));
}); return (
new cljs.core.Var(function(){return clojure.set.superset_QMARK_;},new cljs.core.Symbol("clojure.set","superset?","clojure.set/superset?",(22098740),null),cljs.core.PersistentHashMap.fromArrays([new cljs.core.Keyword(null,"ns","ns",(441598760)),new cljs.core.Keyword(null,"name","name",(1843675177)),new cljs.core.Keyword(null,"file","file",(-1269645878)),new cljs.core.Keyword(null,"end-column","end-column",(1425389514)),new cljs.core.Keyword(null,"column","column",(2078222095)),new cljs.core.Keyword(null,"line","line",(212345235)),new cljs.core.Keyword(null,"end-line","end-line",(1837326455)),new cljs.core.Keyword(null,"arglists","arglists",(1661989754)),new cljs.core.Keyword(null,"doc","doc",(1913296891)),new cljs.core.Keyword(null,"test","test",(577538877))],[cljs.core.with_meta(new cljs.core.Symbol(null,"clojure.set","clojure.set",(-630955632),null),new cljs.core.PersistentArrayMap(null, 2, [new cljs.core.Keyword(null,"author","author",(2111686192)),"Rich Hickey",new cljs.core.Keyword(null,"doc","doc",(1913296891)),"Set operations such as union/intersection."], null)),new cljs.core.Symbol(null,"superset?","superset?",(2074872204),null),null,(16),(1),(137),(137),cljs.core.list(new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Symbol(null,"set1","set1",(-1952562536),null),new cljs.core.Symbol(null,"set2","set2",(1231516531),null)], null)),"Is set1 a superset of set2?",(cljs.core.truth_(clojure.set.superset_QMARK_)?clojure.set.superset_QMARK_.cljs$lang$test:null)])));})()
;
