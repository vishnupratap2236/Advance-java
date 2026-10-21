/* ShopSphere - progressive enhancement helpers (Bootstrap 5 is loaded separately). */
(function () {
    'use strict';

    function csrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.getAttribute('content') : '';
    }

    function showToast(message, variant) {
        var holder = document.querySelector('.ss-toast-holder');
        if (!holder) {
            holder = document.createElement('div');
            holder.className = 'ss-toast-holder';
            document.body.appendChild(holder);
        }
        var toast = document.createElement('div');
        toast.className = 'alert alert-' + (variant || 'info') + ' shadow-sm mb-0 ss-toast';
        toast.setAttribute('role', 'status');
        toast.textContent = message;
        holder.appendChild(toast);
        window.setTimeout(function () {
            toast.style.transition = 'opacity .3s ease';
            toast.style.opacity = '0';
            window.setTimeout(function () {
                toast.remove();
            }, 320);
        }, 3200);
    }

    window.shopSphere = {
        csrfToken: csrfToken,
        showToast: showToast
    };

    /* ---------------------------------------------------- quantity steppers */
    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-qty-step]');
        if (!button) {
            return;
        }
        event.preventDefault();
        var wrapper = button.closest('.ss-qty-box');
        var input = wrapper ? wrapper.querySelector('input') : null;
        if (!input) {
            return;
        }
        var step = parseInt(button.getAttribute('data-qty-step'), 10) || 1;
        var min = parseInt(input.getAttribute('min'), 10);
        var max = parseInt(input.getAttribute('max'), 10);
        var value = parseInt(input.value, 10) || min;
        value += step;
        if (isNaN(min)) { min = 1; }
        if (isNaN(max)) { max = 99; }
        if (value < min) { value = min; }
        if (value > max) { value = max; }
        input.value = value;
    });

    /* -------------------------------------------------- product image gallery */
    document.addEventListener('click', function (event) {
        var thumb = event.target.closest('.ss-gallery-thumb');
        if (!thumb) {
            return;
        }
        var main = document.getElementById('ss-gallery-main-image');
        if (main && thumb.dataset.full) {
            main.src = thumb.dataset.full;
        }
        var thumbs = document.querySelectorAll('.ss-gallery-thumb');
        for (var i = 0; i < thumbs.length; i++) {
            thumbs[i].classList.remove('active');
        }
        thumb.classList.add('active');
    });

    /* ------------------------------------------------- add to cart via fetch */
    document.addEventListener('submit', function (event) {
        var form = event.target.closest('form[data-ajax-cart]');
        if (!form) {
            return;
        }
        event.preventDefault();
        var button = form.querySelector('button[type="submit"]');
        var originalText = button ? button.innerHTML : '';
        if (button) {
            button.disabled = true;
            button.innerHTML = '<span class="ss-spinner"></span> Adding';
        }
        var body = new URLSearchParams(new FormData(form));
        fetch(form.getAttribute('action') || '/api/cart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': csrfToken()
            },
            body: body.toString()
        })
            .then(function (response) {
                return response.json().then(function (data) {
                    return { ok: response.ok, data: data };
                });
            })
            .then(function (result) {
                if (!result.ok) {
                    showToast(result.data && result.data.error ? result.data.error
                        : 'The item could not be added to the cart.', 'danger');
                    return;
                }
                showToast('Added to your cart.', 'success');
                updateCartBadge(result.data.itemCount);
                if (button) {
                    button.disabled = false;
                    button.innerHTML = originalText;
                }
            })
            .catch(function () {
                showToast('The cart could not be reached. Please try again.', 'danger');
                if (button) {
                    button.disabled = false;
                    button.innerHTML = originalText;
                }
            });
    });

    function updateCartBadge(count) {
        var badges = document.querySelectorAll('[data-cart-count]');
        for (var i = 0; i < badges.length; i++) {
            badges[i].textContent = count;
            badges[i].style.display = count > 0 ? '' : 'none';
        }
    }

    /* -------------------------------------------------------- image uploader */
    document.addEventListener('change', function (event) {
        var input = event.target.closest('[data-image-input]');
        if (!input) {
            return;
        }
        var target = document.querySelector(input.getAttribute('data-image-target'));
        if (!target || !input.files || input.files.length === 0) {
            return;
        }
        var file = input.files[0];
        var data = new FormData();
        data.append('file', file);
        var endpoint = input.getAttribute('data-upload-url');
        fetch(endpoint, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-Token': csrfToken()
            },
            body: data
        })
            .then(function (response) { return response.json(); })
            .then(function (result) {
                if (result.error) {
                    showToast(result.error, 'danger');
                    return;
                }
                target.value = result.url;
                showToast('Image uploaded.', 'success');
            })
            .catch(function () {
                showToast('The image could not be uploaded.', 'danger');
            });
    });

    /* ------------------------------------------------- auto submit filters */
    document.addEventListener('change', function (event) {
        var select = event.target.closest('[data-auto-submit]');
        if (select && select.form) {
            select.form.submit();
        }
    });

    /* ------------------------------------------------- dismiss inline alerts */
    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-dismiss-alert]');
        if (!button) {
            return;
        }
        var alert = button.closest('.alert');
        if (alert) {
            alert.remove();
        }
    });
})();
