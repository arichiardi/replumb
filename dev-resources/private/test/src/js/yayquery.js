// example from https://github.com/clojure/clojurescript/wiki/Dependencies

goog.provide('yq');

yq.debugMessage = 'Dead Code';

yq.yayQuery = function() {
    var yay = {};
    yay.sayHello = function(message) {
        console.log(message);
    };
    yay.getMessage = function() {
        return 'Hello, world!';
    };
    return yay;
};
