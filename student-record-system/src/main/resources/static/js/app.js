document.addEventListener('DOMContentLoaded', () => {
    const body = document.body;

    document.querySelectorAll('[data-nav-toggle]').forEach((button) => {
        button.addEventListener('click', () => {
            body.classList.toggle('nav-open');
        });
    });

    document.querySelectorAll('[data-nav-close]').forEach((element) => {
        element.addEventListener('click', () => body.classList.remove('nav-open'));
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            body.classList.remove('nav-open');
        }
    });

    document.querySelectorAll('[data-dismiss-flash]').forEach((button) => {
        button.addEventListener('click', () => {
            const flash = button.closest('.flash');
            if (flash) {
                flash.remove();
            }
        });
    });

    document.querySelectorAll('form[data-confirm]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });

    const marksInput = document.querySelector('[data-marks-input]');
    const preview = document.querySelector('[data-grade-preview]');
    const previewBadge = document.querySelector('[data-grade-badge]');

    if (marksInput && preview && previewBadge) {
        const gradeFor = (marks) => {
            if (marks >= 90) return ['A+', 'grade-a', '10.0 points'];
            if (marks >= 85) return ['A', 'grade-a', '9.0 points'];
            if (marks >= 75) return ['B+', 'grade-b', '8.0 points'];
            if (marks >= 65) return ['B', 'grade-b', '7.0 points'];
            if (marks >= 55) return ['C', 'grade-c', '6.0 points'];
            if (marks >= 45) return ['D', 'grade-d', '5.0 points'];
            return ['F', 'grade-f', '0.0 points'];
        };

        const updatePreview = () => {
            const value = Number.parseFloat(marksInput.value);
            if (Number.isNaN(value) || value < 0 || value > 100) {
                preview.textContent = 'Enter marks to preview';
                previewBadge.textContent = '—';
                previewBadge.className = 'badge grade-none';
                return;
            }
            const [grade, cssClass, points] = gradeFor(value);
            preview.textContent = points;
            previewBadge.textContent = grade;
            previewBadge.className = `badge ${cssClass}`;
        };

        marksInput.addEventListener('input', updatePreview);
        updatePreview();
    }

    window.setTimeout(() => {
        document.querySelectorAll('.flash').forEach((flash) => {
            flash.style.transition = 'opacity 300ms ease, transform 300ms ease';
            flash.style.opacity = '0';
            flash.style.transform = 'translateY(-4px)';
            window.setTimeout(() => flash.remove(), 320);
        });
    }, 7000);
});
