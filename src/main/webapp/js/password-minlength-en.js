(function () {
    var msg = 'Password must be at least 8 characters.';
    document.querySelectorAll('input.ta-pw-min8').forEach(function (el) {
        el.addEventListener('invalid', function () {
            if (el.validity.tooShort) {
                el.setCustomValidity(msg);
            } else {
                el.setCustomValidity('');
            }
        });
        el.addEventListener('input', function () {
            el.setCustomValidity('');
        });
    });
})();
